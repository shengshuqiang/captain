#KeyBoardBinder
自定义弹窗键盘，抽象类，目前IDCardKeyboardBinder（身份证输入法绑定器）为其实现类，

###设计思路
使用Popupwindow承载键盘，使自定义键盘与具体业务界面解耦

自定义弹窗键盘
技术点：
1. 使用Android提供的KeyboardView和Keyboard作为键盘的内容
2. 使用PopupWindow作为载体，增加通用性，不依赖于具体布局
3. 增加键盘弹出动画，在键盘会遮挡EditText时，会同时移动Activity内容区域置键盘上方(doKeyboardShowAnim,doKeyboardDismissAnim)
4. 点击键盘外区域，如果是当前EditText，键盘依旧显示，否则，键盘消失（PopupWindow设置setOutsideTouchable为true，并且增加点击拦截KeyBoardViewTouchInterceptor）
5. 去掉点击EditText时触发系统键盘(EditTextSimplyOnTouchListener)

注意：
EditText一定是在Activity上面的，Dialog或者Popupwindow上面的EditText目前不支持。
1. 按返回键自定义键盘不会消失，需要主动重写Activity的onBackPressed，处理返回事件（虽然）
 @Override
 public void onBackPressed() {
     if (testBuilder != null && testBuilder.isShow()) {
         testBuilder.dismiss();
     } else {
         this.finish();
     }
 }
 注：设置popupWindow.setFocusable(true)能达到功能，但是技术点4无效，得不偿失

 参考：
 http://www.fampennings.nl/maarten/android/09keyboard/index.html

![image](http://git.sankuai.com/projects/HOTEL/repos/hotelplus/browse/simpleblock/sample.png)
    
###使用示例
####参考CustomizeKeyboardActivity
实现IDCardKeyboardBinder接口,将需要弹出自定义键盘的view注册到自定义键盘

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customize_keyboard);

        implKeyboardBinder = new ImplKeyboardBinder(this);
        editText = (EditText) findViewById(R.id.edit1);
    }

    @Override
    protected void onResume() {
        super.onResume();

        registerEditText(editText);
    }

    @Override
    protected void onPause() {
        super.onPause();

        unregisterEditText(editText);
    }

    @Override
    public void onBackPressed() {
        if (!implKeyboardBinder.interceptorOnBackPressed()) {
            // 自定义键盘未拦截返回键
            super.onBackPressed();
        }
    }

    @Override
    public void registerEditText(EditText editText) {
        implKeyboardBinder.registerEditText(editText);
    }

    @Override
    public void unregisterEditText(EditText editText) {
        implKeyboardBinder.unregisterEditText(editText);
    }
    
    
###扩展模块
参考IDCardKeyboardBinder，继承KeyBoardBinder可自定义其他布局键盘
