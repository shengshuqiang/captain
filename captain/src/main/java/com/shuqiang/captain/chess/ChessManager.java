package com.shuqiang.captain.chess;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.github.bhlangonijr.chesslib.Piece;
import com.shuqiang.captain.chess.bt.ChessBluetoothService;
import com.shuqiang.captain.chess.data.ChessAvatarPresets;
import com.shuqiang.captain.chess.data.ChessAvatarStore;
import com.shuqiang.captain.chess.data.ChessHistoryRepository;
import com.shuqiang.captain.chess.data.ChessPrefsStore;
import com.shuqiang.captain.chess.domain.ChessReplayBuilder;
import com.shuqiang.captain.chess.domain.ChessRuleEngine;
import com.shuqiang.captain.chess.model.ChessAvatarPreset;
import com.shuqiang.captain.chess.model.ChessAvatarType;
import com.shuqiang.captain.chess.model.ChessColor;
import com.shuqiang.captain.chess.model.ChessConnectionState;
import com.shuqiang.captain.chess.model.ChessGameRecord;
import com.shuqiang.captain.chess.model.ChessGameResult;
import com.shuqiang.captain.chess.model.ChessMatchSnapshot;
import com.shuqiang.captain.chess.model.ChessMessage;
import com.shuqiang.captain.chess.model.ChessMessageType;
import com.shuqiang.captain.chess.model.ChessMoveOutcome;
import com.shuqiang.captain.chess.model.ChessMoveRecord;
import com.shuqiang.captain.chess.model.ChessReplayFrame;
import com.shuqiang.captain.chess.model.ChessScannedDevice;
import com.shuqiang.captain.chess.model.ChessSeatPreference;
import com.shuqiang.captain.chess.model.ChessSessionRole;
import com.shuqiang.captain.chess.model.ChessTerminationReason;
import com.shuqiang.captain.chess.model.ChessUserProfile;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public class ChessManager implements ChessBluetoothService.Listener {
    private static final String PROTOCOL_VERSION = "1";
    private static final long HEARTBEAT_INTERVAL_MS = 6000L;
    private static volatile ChessManager instance;

    public interface Listener {
        default void onSnapshotChanged(ChessMatchSnapshot snapshot) {
        }

        default void onDiscoveredDevicesChanged(List<ChessScannedDevice> devices) {
        }

        default void onErrorMessage(String message) {
        }

        default void onIncomingDrawOffer(ChessTerminationReason claimReason) {
        }
    }

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final Context appContext;
    private final ChessPrefsStore prefsStore;
    private final ChessAvatarStore avatarStore;
    private final ChessHistoryRepository historyRepository;
    private final ChessReplayBuilder replayBuilder = new ChessReplayBuilder();
    private final ChessBluetoothService bluetoothService;
    private final CopyOnWriteArrayList<Listener> listeners = new CopyOnWriteArrayList<>();
    private final Random random = new Random();
    private ChessRuleEngine ruleEngine = new ChessRuleEngine();
    private ChessMatchSnapshot snapshot = new ChessMatchSnapshot();
    private List<ChessScannedDevice> discoveredDevices = new ArrayList<>();
    private ChessSeatPreference remoteSeatPreference = ChessSeatPreference.RANDOM;
    private boolean localHelloSent;
    private boolean remoteHelloReceived;
    private boolean remoteReadyReceived;
    private boolean gameStarted;
    private boolean gameSaved;
    private long gameStartedAt;
    private final Runnable heartbeatRunnable = new Runnable() {
        @Override
        public void run() {
            if (bluetoothService.isConnected()
                    && snapshot.getConnectionState().ordinal() >= ChessConnectionState.CONNECTED.ordinal()
                    && !snapshot.isFinished()) {
                ChessMessage heartbeat = new ChessMessage();
                heartbeat.type = ChessMessageType.HEARTBEAT;
                heartbeat.gameId = snapshot.getGameId();
                bluetoothService.sendMessage(heartbeat);
                scheduleHeartbeat();
            }
        }
    };

    private ChessManager(Context context) {
        appContext = context.getApplicationContext();
        prefsStore = new ChessPrefsStore(appContext);
        avatarStore = new ChessAvatarStore();
        historyRepository = new ChessHistoryRepository(appContext);
        bluetoothService = new ChessBluetoothService(appContext, this);
        resetToLobbyState();
    }

    public static ChessManager getInstance(Context context) {
        if (instance == null) {
            synchronized (ChessManager.class) {
                if (instance == null) {
                    instance = new ChessManager(context);
                }
            }
        }
        return instance;
    }

    public void registerListener(Listener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
        listener.onSnapshotChanged(snapshot.copy());
        listener.onDiscoveredDevicesChanged(new ArrayList<>(discoveredDevices));
    }

    public void unregisterListener(Listener listener) {
        listeners.remove(listener);
    }

    public ChessUserProfile getLocalProfile() {
        return prefsStore.loadProfile();
    }

    public ChessAvatarStore getAvatarStore() {
        return avatarStore;
    }

    public List<ChessAvatarPreset> getAvatarPresets() {
        return ChessAvatarPresets.getAll();
    }

    public boolean hasCompletedOnboarding() {
        return prefsStore.hasCompletedOnboarding();
    }

    public boolean isBluetoothSupported() {
        return bluetoothService.isBluetoothSupported();
    }

    public BluetoothAdapter getBluetoothAdapter() {
        return bluetoothService.getBluetoothAdapter();
    }

    public ChessMatchSnapshot getSnapshot() {
        return snapshot.copy();
    }

    public List<ChessScannedDevice> getDiscoveredDevices() {
        return new ArrayList<>(discoveredDevices);
    }

    public void saveProfile(ChessUserProfile profile, boolean markCompleted) {
        if (TextUtils.isEmpty(profile.getAvatarValue())) {
            profile.setAvatarType(ChessAvatarType.PRESET);
            profile.setAvatarValue(ChessAvatarPresets.DEFAULT_VALUE);
        }
        prefsStore.saveProfile(profile, markCompleted);
        snapshot.setLocalProfile(profile.copy());
        emitSnapshot();
    }

    public ChessSeatPreference getLastSeatPreference() {
        return prefsStore.getLastSeatPreference();
    }

    public void startHosting(ChessSeatPreference seatPreference) {
        if (!ensureReadyForMatch()) {
            return;
        }
        prepareSession(ChessSessionRole.HOST, seatPreference, "房间已创建，等待附近设备加入");
        bluetoothService.startHosting();
    }

    public void startDiscovery(ChessSeatPreference seatPreference) {
        if (!ensureReadyForMatch()) {
            return;
        }
        prepareSession(ChessSessionRole.PEER, seatPreference, "正在搜索附近的蓝牙设备");
        snapshot.setConnectionState(ChessConnectionState.DISCOVERING);
        emitSnapshot();
        bluetoothService.startDiscovery();
    }

    public void connectToDevice(String address) {
        if (snapshot.getRole() != ChessSessionRole.PEER) {
            startDiscovery(snapshot.getSeatPreference());
        }
        snapshot.setConnectionState(ChessConnectionState.CONNECTING);
        snapshot.setStatusText("正在建立蓝牙连接");
        emitSnapshot();
        prefsStore.setLastDeviceMac(address);
        bluetoothService.connect(address);
    }

    public void cancelSession() {
        bluetoothService.disconnect();
        resetToLobbyState();
    }

    public void submitMove(String from, String to, String promotion) {
        if (snapshot.getConnectionState() != ChessConnectionState.PLAYING || snapshot.isFinished()) {
            emitError("当前不在可走棋状态");
            return;
        }
        if (!snapshot.isMyTurn()) {
            emitError("现在轮到对手走棋");
            return;
        }
        if (snapshot.getRole() == ChessSessionRole.HOST) {
            handleAuthoritativeMove(ruleEngine.applyMove(from, to, promotion), true);
            return;
        }
        if (!ruleEngine.isLegalMove(from, to, promotion)) {
            emitError(ruleEngine.needsPromotion(from, to) && TextUtils.isEmpty(promotion)
                    ? "兵到底线后需要先选择升变棋子"
                    : "当前走法不合法");
            return;
        }
        ChessMessage message = new ChessMessage();
        message.type = ChessMessageType.MOVE_REQUEST;
        message.gameId = snapshot.getGameId();
        message.from = from;
        message.to = to;
        message.promotion = promotion;
        message.clientMoveIndex = snapshot.getMoveCount() + 1;
        bluetoothService.sendMessage(message);
        snapshot.setWaitingForMoveCommit(true);
        updateStatusText();
        emitSnapshot();
    }

    public void offerDraw() {
        if (snapshot.getConnectionState() != ChessConnectionState.PLAYING || snapshot.isFinished()) {
            emitError("当前还不能发起和棋");
            return;
        }
        if (snapshot.isCanClaimDraw() && snapshot.getClaimableDrawReason() != null) {
            requestRuleBasedDraw(snapshot.getClaimableDrawReason());
            return;
        }
        ChessMessage message = new ChessMessage();
        message.type = ChessMessageType.DRAW_OFFER;
        message.gameId = snapshot.getGameId();
        bluetoothService.sendMessage(message);
        snapshot.setOutgoingDrawOfferPending(true);
        snapshot.setStatusText("已发出求和请求，等待对方回应");
        emitSnapshot();
    }

    public void respondToDrawOffer(boolean accept) {
        if (!snapshot.isIncomingDrawOffer()) {
            return;
        }
        snapshot.setIncomingDrawOffer(false);
        ChessMessage response = new ChessMessage();
        response.type = ChessMessageType.DRAW_RESPONSE;
        response.gameId = snapshot.getGameId();
        response.accepted = accept;
        bluetoothService.sendMessage(response);
        if (accept && snapshot.getRole() == ChessSessionRole.HOST) {
            finishGame(ChessGameResult.DRAW, ChessTerminationReason.DRAW_AGREEMENT, true);
            return;
        }
        updateStatusText();
        emitSnapshot();
    }

    public void resign() {
        if (snapshot.getConnectionState() != ChessConnectionState.PLAYING || snapshot.isFinished()) {
            return;
        }
        if (snapshot.getRole() == ChessSessionRole.HOST) {
            ChessColor winner = snapshot.getMyColor().flip();
            finishGame(resultForWinner(winner), ChessTerminationReason.RESIGNATION, true);
            return;
        }
        ChessMessage message = new ChessMessage();
        message.type = ChessMessageType.RESIGN;
        message.gameId = snapshot.getGameId();
        bluetoothService.sendMessage(message);
        snapshot.setStatusText("已发送认输请求，等待房主同步结果");
        emitSnapshot();
    }

    public List<ChessGameRecord> listHistoryGames() {
        return historyRepository.listGames();
    }

    public ChessGameRecord findHistoryGame(String gameId) {
        return historyRepository.findById(gameId);
    }

    public List<ChessReplayFrame> buildReplayFrames(ChessGameRecord record) throws Exception {
        return replayBuilder.buildFrames(record.getPgnText());
    }

    public List<String> getLegalTargets(String from) {
        return ruleEngine.getLegalTargets(from);
    }

    public Piece getPieceAtSquare(String square) {
        return ruleEngine.getPiece(square);
    }

    public boolean needsPromotion(String from, String to) {
        return ruleEngine.needsPromotion(from, to);
    }

    private boolean ensureReadyForMatch() {
        if (!bluetoothService.isBluetoothSupported()) {
            emitError("当前设备不支持蓝牙");
            return false;
        }
        ChessUserProfile profile = prefsStore.loadProfile();
        if (TextUtils.isEmpty(profile.getUsername()) || TextUtils.isEmpty(profile.getAvatarValue())) {
            emitError("请先完成用户名和头像设置");
            return false;
        }
        return true;
    }

    private void prepareSession(ChessSessionRole role, ChessSeatPreference seatPreference, String statusText) {
        bluetoothService.disconnect();
        mainHandler.removeCallbacks(heartbeatRunnable);
        ruleEngine = new ChessRuleEngine();
        remoteSeatPreference = ChessSeatPreference.RANDOM;
        localHelloSent = false;
        remoteHelloReceived = false;
        remoteReadyReceived = false;
        gameStarted = false;
        gameSaved = false;
        gameStartedAt = 0L;
        discoveredDevices = new ArrayList<>();
        ChessMatchSnapshot newSnapshot = new ChessMatchSnapshot();
        newSnapshot.setLocalProfile(prefsStore.loadProfile());
        newSnapshot.setSeatPreference(seatPreference);
        newSnapshot.setRole(role);
        newSnapshot.setConnectionState(role == ChessSessionRole.HOST ? ChessConnectionState.HOSTING : ChessConnectionState.IDLE);
        newSnapshot.setCurrentFen(ruleEngine.getFen());
        newSnapshot.setStatusText(statusText);
        snapshot = newSnapshot;
        prefsStore.setLastSeatPreference(seatPreference);
        emitSnapshot();
        emitDevices();
    }

    private void resetToLobbyState() {
        mainHandler.removeCallbacks(heartbeatRunnable);
        discoveredDevices = new ArrayList<>();
        remoteSeatPreference = ChessSeatPreference.RANDOM;
        localHelloSent = false;
        remoteHelloReceived = false;
        remoteReadyReceived = false;
        gameStarted = false;
        gameSaved = false;
        gameStartedAt = 0L;
        ruleEngine = new ChessRuleEngine();
        ChessMatchSnapshot newSnapshot = new ChessMatchSnapshot();
        newSnapshot.setLocalProfile(prefsStore.loadProfile());
        newSnapshot.setSeatPreference(prefsStore.getLastSeatPreference());
        newSnapshot.setConnectionState(ChessConnectionState.IDLE);
        newSnapshot.setCurrentFen(ruleEngine.getFen());
        newSnapshot.setStatusText("设置资料后，即可发起蓝牙人人对战");
        snapshot = newSnapshot;
        emitSnapshot();
        emitDevices();
    }

    private void sendHelloIfNeeded() {
        if (localHelloSent) {
            return;
        }
        ChessUserProfile profile = prefsStore.loadProfile();
        ChessMessage hello = new ChessMessage();
        hello.type = ChessMessageType.HELLO;
        hello.protocolVersion = PROTOCOL_VERSION;
        hello.username = profile.getUsername();
        hello.avatarType = profile.getAvatarType().name();
        hello.avatar = profile.getAvatarValue();
        hello.seatPreference = snapshot.getSeatPreference().name();
        hello.deviceName = getDeviceName();
        bluetoothService.sendMessage(hello);
        localHelloSent = true;
    }

    private void sendReady() {
        ChessMessage message = new ChessMessage();
        message.type = ChessMessageType.READY;
        message.protocolVersion = PROTOCOL_VERSION;
        bluetoothService.sendMessage(message);
    }

    private void maybeStartGame() {
        if (snapshot.getRole() != ChessSessionRole.HOST || !remoteHelloReceived || !remoteReadyReceived || gameStarted) {
            return;
        }
        gameStarted = true;
        gameStartedAt = System.currentTimeMillis();
        ChessColor hostColor = decideHostColor(snapshot.getSeatPreference(), remoteSeatPreference);
        snapshot.setGameId(UUID.randomUUID().toString());
        snapshot.setConnectionState(ChessConnectionState.PLAYING);
        snapshot.setMyColor(hostColor);
        snapshot.setCurrentFen(ruleEngine.getFen());
        snapshot.setPgnText("");
        updateTurnStatus();
        ChessMessage startGame = new ChessMessage();
        startGame.type = ChessMessageType.START_GAME;
        startGame.gameId = snapshot.getGameId();
        startGame.hostColor = hostColor.name();
        startGame.peerColor = hostColor.flip().name();
        startGame.fen = ruleEngine.getFen();
        bluetoothService.sendMessage(startGame);
        scheduleHeartbeat();
        emitSnapshot();
    }

    private ChessColor decideHostColor(ChessSeatPreference hostPreference, ChessSeatPreference peerPreference) {
        if (hostPreference == ChessSeatPreference.WHITE) {
            return ChessColor.WHITE;
        }
        if (hostPreference == ChessSeatPreference.BLACK) {
            return ChessColor.BLACK;
        }
        if (peerPreference == ChessSeatPreference.WHITE) {
            return ChessColor.BLACK;
        }
        if (peerPreference == ChessSeatPreference.BLACK) {
            return ChessColor.WHITE;
        }
        return random.nextBoolean() ? ChessColor.WHITE : ChessColor.BLACK;
    }

    private void handleAuthoritativeMove(ChessMoveOutcome outcome, boolean fromLocalPlayer) {
        if (!outcome.isApproved()) {
            emitError(outcome.getErrorMessage());
            return;
        }
        applyMoveOutcomeToSnapshot(outcome);
        ChessMoveRecord moveRecord = outcome.getMoveRecord();
        ChessMessage commit = new ChessMessage();
        commit.type = ChessMessageType.MOVE_COMMIT;
        commit.gameId = snapshot.getGameId();
        commit.approved = true;
        commit.uci = moveRecord.getUci();
        commit.san = moveRecord.getSan();
        commit.fen = moveRecord.getFenAfterMove();
        commit.pgn = outcome.getPgnText();
        commit.moveIndex = moveRecord.getMoveIndex();
        commit.activeColor = moveRecord.getActiveColor().name();
        if (outcome.isFinished()) {
            commit.result = outcome.getResult().name();
            commit.terminationReason = outcome.getTerminationReason().name();
        }
        if (!fromLocalPlayer || snapshot.getRole() == ChessSessionRole.HOST) {
            bluetoothService.sendMessage(commit);
        }
        if (outcome.isFinished()) {
            finishGame(outcome.getResult(), outcome.getTerminationReason(), false);
        } else {
            updateTurnStatus();
            emitSnapshot();
        }
    }

    private void applyMoveOutcomeToSnapshot(ChessMoveOutcome outcome) {
        ChessMoveRecord moveRecord = outcome.getMoveRecord();
        snapshot.getMoveRecords().clear();
        snapshot.getMoveRecords().addAll(ruleEngine.getMoveRecords());
        snapshot.setMoveCount(ruleEngine.getMoveCount());
        snapshot.setCurrentFen(moveRecord.getFenAfterMove());
        snapshot.setLastMoveUci(moveRecord.getUci());
        snapshot.setPgnText(outcome.getPgnText());
        snapshot.setWaitingForMoveCommit(false);
        snapshot.setCanClaimDraw(outcome.isCanClaimDraw());
        snapshot.setClaimableDrawReason(outcome.getClaimableDrawReason());
        snapshot.setIncomingDrawOffer(false);
        snapshot.setOutgoingDrawOfferPending(false);
        snapshot.setInCheck(ruleEngine.isKingAttacked());
    }

    private void requestRuleBasedDraw(ChessTerminationReason requestedReason) {
        if (snapshot.getRole() == ChessSessionRole.HOST) {
            ChessMoveOutcome claimOutcome = ruleEngine.claimDrawIfPossible();
            if (!claimOutcome.isApproved()) {
                emitError(claimOutcome.getErrorMessage());
                return;
            }
            finishGame(ChessGameResult.DRAW, requestedReason, true);
            return;
        }
        ChessMessage message = new ChessMessage();
        message.type = ChessMessageType.DRAW_OFFER;
        message.gameId = snapshot.getGameId();
        message.terminationReason = requestedReason.name();
        bluetoothService.sendMessage(message);
        snapshot.setStatusText("已按规则申请和棋，等待房主裁定");
        emitSnapshot();
    }

    private void finishGame(ChessGameResult result, ChessTerminationReason terminationReason, boolean sendEndGameMessage) {
        snapshot.setConnectionState(ChessConnectionState.FINISHED);
        snapshot.setFinished(true);
        snapshot.setResult(result);
        snapshot.setTerminationReason(terminationReason);
        snapshot.setWaitingForMoveCommit(false);
        snapshot.setIncomingDrawOffer(false);
        snapshot.setOutgoingDrawOfferPending(false);
        snapshot.setCanClaimDraw(false);
        snapshot.setClaimableDrawReason(null);
        snapshot.setStatusText(ChessTextFormatter.combineResultAndReason(result, terminationReason, snapshot.getMyColor()));
        if (sendEndGameMessage && bluetoothService.isConnected()) {
            ChessMessage endGame = new ChessMessage();
            endGame.type = ChessMessageType.END_GAME;
            endGame.gameId = snapshot.getGameId();
            endGame.result = result.name();
            endGame.terminationReason = terminationReason.name();
            endGame.fen = ruleEngine.getFen();
            endGame.pgn = ruleEngine.getPgnText();
            endGame.moveIndex = ruleEngine.getMoveCount();
            bluetoothService.sendMessage(endGame);
        }
        saveGameIfNecessary(result, terminationReason);
        mainHandler.removeCallbacks(heartbeatRunnable);
        emitSnapshot();
    }

    private void updateTurnStatus() {
        snapshot.setMyTurn(snapshot.getMyColor() != null && snapshot.getMyColor() == ruleEngine.getActiveColor());
        snapshot.setInCheck(ruleEngine.isKingAttacked());
        snapshot.setCanClaimDraw(ruleEngine.canClaimThreefoldRepetition() || ruleEngine.canClaimFiftyMoveDraw());
        if (ruleEngine.canClaimThreefoldRepetition()) {
            snapshot.setClaimableDrawReason(ChessTerminationReason.THREEFOLD_REPETITION_CLAIM);
        } else if (ruleEngine.canClaimFiftyMoveDraw()) {
            snapshot.setClaimableDrawReason(ChessTerminationReason.FIFTY_MOVE_CLAIM);
        } else {
            snapshot.setClaimableDrawReason(null);
        }
        updateStatusText();
    }

    private void updateStatusText() {
        if (snapshot.isFinished()) {
            snapshot.setStatusText(ChessTextFormatter.combineResultAndReason(snapshot.getResult(),
                    snapshot.getTerminationReason(), snapshot.getMyColor()));
            return;
        }
        if (snapshot.isWaitingForMoveCommit()) {
            snapshot.setStatusText("已提交走子，等待房主确认");
            return;
        }
        if (snapshot.isIncomingDrawOffer()) {
            snapshot.setStatusText("对手发起了求和，请做决定");
            return;
        }
        if (snapshot.getConnectionState() == ChessConnectionState.PLAYING) {
            StringBuilder statusBuilder = new StringBuilder(snapshot.isMyTurn() ? "轮到你走棋" : "等待对手走棋");
            if (snapshot.isInCheck()) {
                statusBuilder.append(snapshot.isMyTurn() ? "，你正在被将军" : "，对手正在被将军");
            } else if (snapshot.isCanClaimDraw()) {
                statusBuilder.append("，当前满足和棋申请条件");
            }
            snapshot.setStatusText(statusBuilder.toString());
            return;
        }
        if (snapshot.getConnectionState() == ChessConnectionState.CONNECTED) {
            snapshot.setStatusText("蓝牙已连接，正在交换资料");
            return;
        }
        if (snapshot.getConnectionState() == ChessConnectionState.CONNECTING) {
            snapshot.setStatusText("正在连接对手设备");
        }
    }

    private void saveGameIfNecessary(ChessGameResult result, ChessTerminationReason terminationReason) {
        if (gameSaved || TextUtils.isEmpty(snapshot.getGameId()) || snapshot.getOpponentProfile() == null) {
            return;
        }
        ChessGameRecord record = new ChessGameRecord();
        record.setGameId(snapshot.getGameId());
        record.setStartedAt(gameStartedAt == 0L ? System.currentTimeMillis() : gameStartedAt);
        record.setEndedAt(System.currentTimeMillis());
        record.setMyUsername(snapshot.getLocalProfile().getUsername());
        record.setMyAvatarType(snapshot.getLocalProfile().getAvatarType());
        record.setMyAvatarValue(snapshot.getLocalProfile().getAvatarValue());
        record.setOpponentUsername(snapshot.getOpponentProfile().getUsername());
        record.setOpponentAvatarType(snapshot.getOpponentProfile().getAvatarType());
        record.setOpponentAvatarValue(snapshot.getOpponentProfile().getAvatarValue());
        record.setMyColor(snapshot.getMyColor() == null ? ChessColor.WHITE : snapshot.getMyColor());
        record.setResult(result);
        record.setTerminationReason(terminationReason);
        record.setMoveCount(ruleEngine.getMoveCount());
        record.setFinalFen(ruleEngine.getFen());
        record.setPgnText(ruleEngine.getPgnText());
        record.setAborted(result == ChessGameResult.ABORTED || terminationReason == ChessTerminationReason.ABORTED);
        historyRepository.saveGame(record);
        gameSaved = true;
    }

    private ChessGameResult resultForWinner(ChessColor winner) {
        return winner == ChessColor.WHITE ? ChessGameResult.WHITE_WIN : ChessGameResult.BLACK_WIN;
    }

    private String getDeviceName() {
        BluetoothAdapter adapter = bluetoothService.getBluetoothAdapter();
        if (adapter == null) {
            return "Captain Device";
        }
        try {
            String deviceName = adapter.getName();
            return TextUtils.isEmpty(deviceName) ? "Captain Device" : deviceName;
        } catch (SecurityException ignore) {
            return "Captain Device";
        }
    }

    private void emitSnapshot() {
        ChessMatchSnapshot copy = snapshot.copy();
        for (Listener listener : listeners) {
            listener.onSnapshotChanged(copy);
        }
    }

    private void emitDevices() {
        List<ChessScannedDevice> copy = new ArrayList<>(discoveredDevices);
        for (Listener listener : listeners) {
            listener.onDiscoveredDevicesChanged(copy);
        }
    }

    private void emitError(String message) {
        for (Listener listener : listeners) {
            listener.onErrorMessage(message);
        }
    }

    private void scheduleHeartbeat() {
        mainHandler.removeCallbacks(heartbeatRunnable);
        mainHandler.postDelayed(heartbeatRunnable, HEARTBEAT_INTERVAL_MS);
    }

    @Override
    public void onDiscoveryDevicesChanged(List<ChessScannedDevice> devices) {
        mainHandler.post(() -> {
            discoveredDevices = devices;
            emitDevices();
        });
    }

    @Override
    public void onSocketConnected(boolean asHost, String remoteDeviceName) {
        mainHandler.post(() -> {
            snapshot.setConnectionState(ChessConnectionState.CONNECTED);
            snapshot.setRemoteDeviceName(remoteDeviceName);
            updateStatusText();
            emitSnapshot();
            sendHelloIfNeeded();
            scheduleHeartbeat();
        });
    }

    @Override
    public void onMessageReceived(ChessMessage message) {
        mainHandler.post(() -> handleIncomingMessage(message));
    }

    @Override
    public void onConnectionLost(String reason) {
        mainHandler.post(() -> {
            if (snapshot.getConnectionState() == ChessConnectionState.IDLE || snapshot.isFinished()) {
                return;
            }
            if (snapshot.getConnectionState() == ChessConnectionState.PLAYING && !snapshot.isFinished()) {
                finishGame(ChessGameResult.ABORTED, ChessTerminationReason.ABORTED, false);
            } else {
                snapshot.setConnectionState(ChessConnectionState.DISCONNECTED);
                snapshot.setStatusText(TextUtils.isEmpty(reason) ? "蓝牙连接已断开" : reason);
                emitSnapshot();
            }
        });
    }

    @Override
    public void onError(String errorMessage) {
        mainHandler.post(() -> emitError(errorMessage));
    }

    private void handleIncomingMessage(ChessMessage message) {
        if (message == null || message.type == null) {
            return;
        }
        switch (message.type) {
            case HELLO:
                remoteHelloReceived = true;
                snapshot.setOpponentProfile(new ChessUserProfile(message.username,
                        ChessAvatarType.fromValue(message.avatarType), message.avatar));
                remoteSeatPreference = ChessSeatPreference.fromValue(message.seatPreference);
                sendReady();
                maybeStartGame();
                emitSnapshot();
                break;
            case READY:
                remoteReadyReceived = true;
                maybeStartGame();
                break;
            case START_GAME:
                gameStarted = true;
                gameStartedAt = System.currentTimeMillis();
                snapshot.setGameId(message.gameId);
                snapshot.setConnectionState(ChessConnectionState.PLAYING);
                snapshot.setMyColor(snapshot.getRole() == ChessSessionRole.HOST
                        ? ChessColor.valueOf(message.hostColor)
                        : ChessColor.valueOf(message.peerColor));
                snapshot.setCurrentFen(message.fen);
                updateTurnStatus();
                emitSnapshot();
                break;
            case MOVE_REQUEST:
                handleMoveRequest(message);
                break;
            case MOVE_COMMIT:
                handleMoveCommit(message);
                break;
            case DRAW_OFFER:
                handleDrawOffer(message);
                break;
            case DRAW_RESPONSE:
                handleDrawResponse(message);
                break;
            case RESIGN:
                if (snapshot.getRole() == ChessSessionRole.HOST) {
                    finishGame(resultForWinner(snapshot.getMyColor()), ChessTerminationReason.RESIGNATION, true);
                }
                break;
            case SYNC_STATE:
                syncStateFromRemote(message);
                break;
            case HEARTBEAT:
                break;
            case END_GAME:
                handleEndGame(message);
                break;
            default:
                break;
        }
    }

    private void handleMoveRequest(ChessMessage message) {
        if (snapshot.getRole() != ChessSessionRole.HOST) {
            return;
        }
        if (!isCurrentGameMessage(message)) {
            sendRejectedMoveCommit("对局状态已变更，请重新同步棋盘");
            return;
        }
        if (snapshot.getConnectionState() != ChessConnectionState.PLAYING || snapshot.isFinished()) {
            sendRejectedMoveCommit("当前对局尚未进入可走棋状态");
            return;
        }
        if (snapshot.getMyColor() == null || ruleEngine.getActiveColor() != snapshot.getMyColor().flip()) {
            sendRejectedMoveCommit("当前不是你的回合");
            return;
        }
        if (message.clientMoveIndex != null && message.clientMoveIndex != snapshot.getMoveCount() + 1) {
            sendRejectedMoveCommit("你的棋局状态已过期，请先同步后再走棋");
            return;
        }
        handleAuthoritativeMove(ruleEngine.applyMove(message.from, message.to, message.promotion), false);
    }

    private void handleMoveCommit(ChessMessage message) {
        if (!isCurrentGameMessage(message)) {
            return;
        }
        snapshot.setWaitingForMoveCommit(false);
        if (message.approved != null && !message.approved) {
            String rejectionMessage = TextUtils.isEmpty(message.statusMessage) ? "本次走子被房主拒绝" : message.statusMessage;
            if (hasRemoteBoardState(message)) {
                syncStateFromRemote(message, rejectionMessage);
            } else {
                snapshot.setStatusText(rejectionMessage);
                emitSnapshot();
            }
            return;
        }
        if (snapshot.getRole() == ChessSessionRole.PEER) {
            String uci = message.uci == null ? "" : message.uci;
            if (uci.length() < 4) {
                emitError("收到的走子提交缺少坐标信息");
                return;
            }
            ChessMoveOutcome outcome = ruleEngine.applyMove(uci.substring(0, 2), uci.substring(2, 4),
                    uci.length() > 4 ? uci.substring(4, 5) : null);
            if (!outcome.isApproved()) {
                syncStateFromRemote(message);
                return;
            }
            applyMoveOutcomeToSnapshot(outcome);
        }
        if (!TextUtils.isEmpty(message.result) && !TextUtils.isEmpty(message.terminationReason)) {
            finishGame(ChessGameResult.valueOf(message.result),
                    ChessTerminationReason.valueOf(message.terminationReason), false);
        } else {
            updateTurnStatus();
            emitSnapshot();
        }
    }

    private void handleDrawOffer(ChessMessage message) {
        if (!TextUtils.isEmpty(message.terminationReason)) {
            ChessTerminationReason requestedReason = ChessTerminationReason.valueOf(message.terminationReason);
            ChessMoveOutcome claimOutcome = ruleEngine.claimDrawIfPossible();
            if (snapshot.getRole() == ChessSessionRole.HOST && claimOutcome.isApproved()) {
                finishGame(ChessGameResult.DRAW, requestedReason, true);
                return;
            }
            ChessMessage response = new ChessMessage();
            response.type = ChessMessageType.DRAW_RESPONSE;
            response.gameId = snapshot.getGameId();
            response.accepted = false;
            response.statusMessage = "当前局面暂不满足和棋申请条件";
            bluetoothService.sendMessage(response);
            return;
        }
        snapshot.setIncomingDrawOffer(true);
        updateStatusText();
        emitSnapshot();
        for (Listener listener : listeners) {
            listener.onIncomingDrawOffer(null);
        }
    }

    private void handleDrawResponse(ChessMessage message) {
        snapshot.setOutgoingDrawOfferPending(false);
        if (message.accepted != null && message.accepted) {
            if (snapshot.getRole() == ChessSessionRole.HOST) {
                finishGame(ChessGameResult.DRAW, ChessTerminationReason.DRAW_AGREEMENT, true);
                return;
            }
            snapshot.setStatusText("对方同意和棋，等待房主同步终局");
            emitSnapshot();
            return;
        }
        snapshot.setStatusText(TextUtils.isEmpty(message.statusMessage) ? "对方拒绝了求和请求" : message.statusMessage);
        emitSnapshot();
    }

    private void syncStateFromRemote(ChessMessage message) {
        syncStateFromRemote(message, null);
    }

    private void syncStateFromRemote(ChessMessage message, String statusOverride) {
        try {
            if (TextUtils.isEmpty(message.pgn)) {
                if (message.moveIndex != null && message.moveIndex > 0) {
                    emitError("对局状态同步失败");
                    return;
                }
                ruleEngine.reset();
            } else {
                ruleEngine.restoreFromSan(message.pgn);
            }
            snapshot.getMoveRecords().clear();
            snapshot.getMoveRecords().addAll(ruleEngine.getMoveRecords());
            snapshot.setMoveCount(ruleEngine.getMoveCount());
            snapshot.setCurrentFen(ruleEngine.getFen());
            snapshot.setPgnText(ruleEngine.getPgnText());
            snapshot.setLastMoveUci(ruleEngine.getMoveCount() == 0 ? null
                    : ruleEngine.getMoveRecords().get(ruleEngine.getMoveCount() - 1).getUci());
            snapshot.setWaitingForMoveCommit(false);
            snapshot.setIncomingDrawOffer(false);
            snapshot.setOutgoingDrawOfferPending(false);
            updateTurnStatus();
            if (!TextUtils.isEmpty(statusOverride)) {
                snapshot.setStatusText(statusOverride);
            }
            emitSnapshot();
        } catch (Exception exception) {
            emitError("同步棋局失败: " + exception.getMessage());
        }
    }

    private void handleEndGame(ChessMessage message) {
        if (!TextUtils.isEmpty(message.pgn)) {
            syncStateFromRemote(message);
        }
        finishGame(ChessGameResult.valueOf(message.result),
                ChessTerminationReason.valueOf(message.terminationReason), false);
    }

    private boolean isCurrentGameMessage(ChessMessage message) {
        if (message == null) {
            return false;
        }
        if (TextUtils.isEmpty(snapshot.getGameId())) {
            return TextUtils.isEmpty(message.gameId);
        }
        return TextUtils.equals(snapshot.getGameId(), message.gameId);
    }

    private boolean hasRemoteBoardState(ChessMessage message) {
        return message != null
                && (!TextUtils.isEmpty(message.pgn)
                || !TextUtils.isEmpty(message.fen)
                || (message.moveIndex != null && message.moveIndex >= 0));
    }

    private void sendRejectedMoveCommit(String statusMessage) {
        ChessMessage response = new ChessMessage();
        response.type = ChessMessageType.MOVE_COMMIT;
        response.gameId = snapshot.getGameId();
        response.approved = false;
        response.statusMessage = statusMessage;
        response.fen = ruleEngine.getFen();
        response.pgn = ruleEngine.getPgnText();
        response.moveIndex = ruleEngine.getMoveCount();
        response.activeColor = ruleEngine.getActiveColor().name();
        bluetoothService.sendMessage(response);
    }
}
