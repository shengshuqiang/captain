package com.shuqiang.captain;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.captain.base.BaseActivity;
import com.captain.base.PermissionUtils;

import java.util.ArrayList;

import captain.R;
import devlight.io.library.ntb.NavigationTabBar;

public class MainActivity extends BaseActivity {
    // 主橱窗 Tab
    private final String MAIN_SHOP_TAB = "MainShopTab";
    // 我的橱窗 Tab
    private final String ME_TAB = "MeTab";
    // Tabs
    private final String[] TABS = { MAIN_SHOP_TAB, ME_TAB };
    private MainShopTabView mainShopTabView;
    private MeTabView meTabView;
    private int tabBarBaseBottomMargin = Integer.MIN_VALUE;
    private int viewPagerBaseBottomMargin = Integer.MIN_VALUE;
    private int contentBottomInset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initUI();
    }

    @Override
    protected int getContentViewResource() {
        return R.layout.activity_content;
    }

    @Override
    protected void initToolbar(View appBarLayout, Toolbar toolbar) {
        super.initToolbar(appBarLayout, toolbar);
        updateShellTitle(0);
    }

    private void initUI() {
        final ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
        viewPager.setAdapter(new PagerAdapter() {

            @Override
            public int getCount() {
                return TABS.length;
            }

            @Override
            public boolean isViewFromObject(final View view, final Object object) {
                return view.equals(object);
            }

            @Override
            public void destroyItem(final View container, final int position, final Object object) {
                View view = (View) object;
                ((ViewPager) container).removeView(view);
                if (view == mainShopTabView) {
                    mainShopTabView = null;
                } else if (view == meTabView) {
                    meTabView = null;
                }
            }

            @Override
            public Object instantiateItem(final ViewGroup container, final int position) {
                View view;
                switch (TABS[position]) {
                    case MAIN_SHOP_TAB:
                        // 主橱窗 Tab
                        if (mainShopTabView == null) {
                            mainShopTabView = new MainShopTabView(MainActivity.this);
                        }
                        mainShopTabView.setBottomContentInset(contentBottomInset);
                        detachFromParent(mainShopTabView);
                        container.addView(mainShopTabView, new ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT));
                        view = mainShopTabView;
                        break;
                    case ME_TAB:
                        // 我的 Tab
                        if (meTabView == null) {
                            meTabView = new MeTabView(MainActivity.this);
                        }
                        detachFromParent(meTabView);
                        container.addView(meTabView, new ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT));
                        view = meTabView;
                        break;
                    default:
                        view = null;
                        break;
                }
                return view;
            }
        });

        final NavigationTabBar navigationTabBar = (NavigationTabBar) findViewById(R.id.tab_bar);
        final ArrayList<NavigationTabBar.Model> models = new ArrayList<>();
        models.add(
                new NavigationTabBar.Model.Builder(
                        getResources().getDrawable(R.drawable.tools),
                        ContextCompat.getColor(this, R.color.captain_tab_home))
                        .title(getString(R.string.captain_shell_title_home))
                        .build()
        );
        models.add(
                new NavigationTabBar.Model.Builder(
                        getResources().getDrawable(R.drawable.person),
                        ContextCompat.getColor(this, R.color.captain_tab_profile))
                        .title(getString(R.string.captain_shell_title_profile))
                        .build()
        );
        navigationTabBar.setModels(models);
        navigationTabBar.setViewPager(viewPager, 0);
        bindBottomInset(viewPager, navigationTabBar);

        //IMPORTANT: ENABLE SCROLL BEHAVIOUR IN COORDINATOR LAYOUT
        navigationTabBar.setBehaviorEnabled(true);

        navigationTabBar.setOnTabBarSelectedIndexListener(new NavigationTabBar.OnTabBarSelectedIndexListener() {
            @Override
            public void onStartTabSelected(final NavigationTabBar.Model model, final int index) {
            }

            @Override
            public void onEndTabSelected(final NavigationTabBar.Model model, final int index) {
                model.hideBadge();
                updateShellTitle(index);
                if (index == 0) {
                    refreshHomeTab();
                }
            }
        });
        navigationTabBar.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(final int position, final float positionOffset, final int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(final int position) {
                updateShellTitle(position);
                if (position == 0) {
                    refreshHomeTab();
                }
            }

            @Override
            public void onPageScrollStateChanged(final int state) {

            }
        });
        // 触发隐私模式弹窗
        PermissionUtils.showPolicyDialog(this);

    }

    private void updateShellTitle(int position) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar == null) {
            return;
        }
        if (position == 1) {
            actionBar.setTitle(R.string.captain_shell_title_profile);
        } else {
            actionBar.setTitle(R.string.captain_shell_title_home);
        }
    }

    public void refreshHomeTab() {
        if (mainShopTabView != null) {
            mainShopTabView.refreshVisibleItems();
        }
    }

    // 隐藏功能开启后切回首页，确保新增入口立即可见。
    public void switchToHomeTab() {
        ViewPager viewPager = findViewById(R.id.view_pager);
        if (viewPager == null) {
            return;
        }
        if (viewPager.getCurrentItem() != 0) {
            viewPager.setCurrentItem(0, false);
            return;
        }
        updateShellTitle(0);
        refreshHomeTab();
    }

    private void detachFromParent(View view) {
        ViewParent parent = view.getParent();
        if (parent instanceof ViewGroup) {
            ((ViewGroup) parent).removeView(view);
        }
    }

    // 底部浮层 TabBar 需要和系统导航区一起避让，避免页面末尾被遮挡后无法继续滚动。
    private void bindBottomInset(ViewPager viewPager, NavigationTabBar navigationTabBar) {
        View root = (View) viewPager.getParent();
        if (root == null) {
            return;
        }
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            updateBottomSpacing(viewPager, navigationTabBar, insets == null ? 0 : insets.getSystemWindowInsetBottom());
            return insets;
        });
        navigationTabBar.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            if ((bottom - top) != (oldBottom - oldTop)) {
                WindowInsetsCompat insets = ViewCompat.getRootWindowInsets(root);
                updateBottomSpacing(viewPager, navigationTabBar, insets == null ? 0 : insets.getSystemWindowInsetBottom());
            }
        });
        root.post(() -> {
            WindowInsetsCompat insets = ViewCompat.getRootWindowInsets(root);
            updateBottomSpacing(viewPager, navigationTabBar, insets == null ? 0 : insets.getSystemWindowInsetBottom());
            ViewCompat.requestApplyInsets(root);
        });
    }

    private void updateBottomSpacing(ViewPager viewPager, NavigationTabBar navigationTabBar, int navigationBarInset) {
        ViewGroup.MarginLayoutParams tabBarLayoutParams =
                (ViewGroup.MarginLayoutParams) navigationTabBar.getLayoutParams();
        if (tabBarBaseBottomMargin == Integer.MIN_VALUE) {
            tabBarBaseBottomMargin = tabBarLayoutParams.bottomMargin;
        }
        int targetTabBarBottomMargin = tabBarBaseBottomMargin + navigationBarInset;
        if (tabBarLayoutParams.bottomMargin != targetTabBarBottomMargin) {
            tabBarLayoutParams.bottomMargin = targetTabBarBottomMargin;
            navigationTabBar.setLayoutParams(tabBarLayoutParams);
        }

        ViewGroup.MarginLayoutParams viewPagerLayoutParams =
                (ViewGroup.MarginLayoutParams) viewPager.getLayoutParams();
        if (viewPagerBaseBottomMargin == Integer.MIN_VALUE) {
            viewPagerBaseBottomMargin = viewPagerLayoutParams.bottomMargin;
        }
        int tabBarHeight = navigationTabBar.getHeight();
        if (tabBarHeight <= 0) {
            tabBarHeight = getResources().getDimensionPixelSize(R.dimen.captain_size_tab_height);
        }
        contentBottomInset = tabBarHeight + targetTabBarBottomMargin;
        if (mainShopTabView != null) {
            mainShopTabView.setBottomContentInset(contentBottomInset);
        }
        int targetViewPagerBottomMargin = viewPagerBaseBottomMargin + contentBottomInset;
        if (viewPagerLayoutParams.bottomMargin != targetViewPagerBottomMargin) {
            viewPagerLayoutParams.bottomMargin = targetViewPagerBottomMargin;
            viewPager.setLayoutParams(viewPagerLayoutParams);
        }
    }
}
