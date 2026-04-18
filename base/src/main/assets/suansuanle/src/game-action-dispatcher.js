/**
 * 所有输入入口都只走这一层，避免按钮、物理键盘、语音出现分叉逻辑。
 */
export class GameActionDispatcher {
  constructor(handler) {
    this.handler = handler;
  }

  dispatch(action) {
    if (!action || !action.type || typeof this.handler !== "function") {
      return;
    }
    this.handler(action);
  }

  digit(value) {
    this.dispatch({ type: "digit", value: String(value) });
  }

  backspace() {
    this.dispatch({ type: "backspace" });
  }

  submit() {
    this.dispatch({ type: "submit" });
  }
}
