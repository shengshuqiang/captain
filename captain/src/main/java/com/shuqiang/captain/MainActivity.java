package com.shuqiang.captain;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.captain.base.BaseActivity;
import com.captain.base.PermissionUtils;
import com.captain.base.Utils;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initUI();
    }

    @Override
    protected int getContentViewResource() {
        return R.layout.activity_content;
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
                ((ViewPager) container).removeView((View) object);
            }

            @Override
            public Object instantiateItem(final ViewGroup container, final int position) {
                View view;
                switch (TABS[position]) {
                    case MAIN_SHOP_TAB:
                        // 主橱窗 Tab
                        MainShopTabView mainShopTabView = new MainShopTabView(getBaseContext());
                        container.addView(mainShopTabView, new ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT));
                        view = mainShopTabView;
                        break;
                    case ME_TAB:
                        // 我的 Tab
                        MeTabView meTabView = new MeTabView(getBaseContext());
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

        final String[] colors = getResources().getStringArray(R.array.default_preview);

        final NavigationTabBar navigationTabBar = (NavigationTabBar) findViewById(R.id.tab_bar);
        final ArrayList<NavigationTabBar.Model> models = new ArrayList<>();
        models.add(
                new NavigationTabBar.Model.Builder(
                        getResources().getDrawable(R.drawable.tools),
                        Color.parseColor(colors[0]))
                        .title("首页")
                        .build()
        );
        models.add(
                new NavigationTabBar.Model.Builder(
                        getResources().getDrawable(R.drawable.person),
                        Color.parseColor(colors[4]))
                        .title("我的")
                        .build()
        );
        navigationTabBar.setModels(models);
        navigationTabBar.setViewPager(viewPager, 0);

        //IMPORTANT: ENABLE SCROLL BEHAVIOUR IN COORDINATOR LAYOUT
        navigationTabBar.setBehaviorEnabled(true);

        navigationTabBar.setOnTabBarSelectedIndexListener(new NavigationTabBar.OnTabBarSelectedIndexListener() {
            @Override
            public void onStartTabSelected(final NavigationTabBar.Model model, final int index) {
            }

            @Override
            public void onEndTabSelected(final NavigationTabBar.Model model, final int index) {
                model.hideBadge();
            }
        });
        navigationTabBar.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(final int position, final float positionOffset, final int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(final int position) {

            }

            @Override
            public void onPageScrollStateChanged(final int state) {

            }
        });
        // 触发隐私模式弹窗
        PermissionUtils.showPolicyDialog(this);

    }
}