<img src="./screenshot/icon.png">

# 应用简介

Android 平台在线漫画阅读器  
Online manga reader based on Android

[![Build Status](https://travis-ci.com/Haleydu/Cimoc.svg?branch=release-tci)](https://travis-ci.com/github/Haleydu/Cimoc)
[![codebeat badge](https://codebeat.co/badges/d8389768-fbb1-428b-b02b-5add8317057a)](https://codebeat.co/projects/github-com-haleydu-cimoc-release-tci)
[![GitHub release](https://img.shields.io/github/release/Haleydu/Cimoc.svg)](https://github.com/Haleydu/Cimoc/releases)
[![Join the chat at https://gitter.im/Haleydu_Cimoc/community](https://badges.gitter.im/Haleydu_Cimoc/community.svg)](https://gitter.im/Haleydu_Cimoc/community?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![](https://img.shields.io/github/downloads/Haleydu/cimoc/total.svg)](https://github.com/Haleydu/Cimoc/releases)

# 下载
> 所有release由`travis-ci`编译发布，如果在release界面某个版本没有apk，那么要么是正在编译，要么就是编译失败了
> 使用`pre-release`版本会在每次启动时显示检查更新提示。
> 安卓apk下载地址[软件下载](https://github.com/Haleydu/Cimoc/releases)


# 漫画源
> 漫画源工作情况可以在[project](https://github.com/Haleydu/Cimoc/projects/1)中进行查看，请尽量不要重复issues
> 各位大佬们提交漫画源相关issue请按照[模板](https://github.com/Haleydu/Cimoc/issues/new?assignees=&labels=%E6%BC%AB%E7%94%BB%E6%BA%90%E9%97%AE%E9%A2%98&template=comic-source-issues.md&title=%5BCS%5D)填写，方便检查问题。

# 功能简介
- 翻页阅读（Page Reader）
- 卷纸阅读（Scroll Reader）
- 检查漫画更新（Check Manga For Update）
- 下载漫画（Download Manga）
- 本地漫画（Local Reader）
- 本地备份恢复（Local Backup）

# 软件使用说明
- 安装完成后，直接点击右上角的搜索，即可搜索到漫画
- 全部代码我都自己分析了一遍没有后门，没有木马，没有病毒，代码是开源的大家也可以帮忙分析
- 软件会被腾讯报毒，信不过本作者的可以下载apk后，进行在线查毒分析
- 代码是开源的，apk是travis自动编译发布的，不放心的可以自己编译apk安装
- 后期有时间会把被误报病毒的那些代码处理了。

# 感谢以下的开源项目及作者
- [Android Open Source Project](http://source.android.com/)
- [ButterKnife](https://github.com/JakeWharton/butterknife)
- [GreenDAO](https://github.com/greenrobot/greenDAO)
- [OkHttp](https://github.com/square/okhttp)
- [Fresco](https://github.com/facebook/fresco)
- [Jsoup](https://github.com/jhy/jsoup)
- [DiscreteSeekBar](https://github.com/AnderWeb/discreteSeekBar)
- [RxJava](https://github.com/ReactiveX/RxJava)
- [RxAndroid](https://github.com/ReactiveX/RxAndroid)
- [RecyclerViewPager](https://github.com/lsjwzh/RecyclerViewPager)
- [PhotoDraweeView](https://github.com/ongakuer/PhotoDraweeView)
- [Rhino](https://github.com/mozilla/rhino)
- [BlazingChain](https://github.com/tommyettinger/BlazingChain)
- [AppUpdater](https://gitee.com/jenly1314/AppUpdater)


# 应用截图
<img src="./screenshot/01.png" width="250">

# 增加图源（欢迎pr）
- 继承 MangaParser 类，参照 Parser 接口的注释
> 在app\src\main\java\com\hiroshi\cimoc\source目录里面随便找一个复制一下
> 注释是这个：app\src\main\java\com\hiroshi\cimoc\parser\MangaParser.java
- （可选）继承 MangaCategory 类，参照 Category 接口的注释
> 这个没什么大用的感觉，个人不常用，直接删掉不会有什么影响
- 在 SourceManger 的 getParser() 方法中加入相应分支
> case 里面无脑添加
- 在 UpdateHelper 的 initSource() 方法中初始化图源

## cimoc设置
- 阅读模式－翻页模式-卷纸模式
- 定义点击事件－左上－上一页，右下下一页
- 自定义长按点击事件-切换阅读模式
- 启用快速翻页（减少）
- 自动裁剪百边
- 禁止双击放大
- 启用通知栏
## boox优化配置
- dpi－424
- 字体加粗－
- a2刷新－打开
- 动画过滤200
- 全刷触发20
- 全屏－关闭

# 软件更新方向：
- 能正常搜索解析网络上大部分免费的漫画
- 界面简洁为主
- 解决apk影响体验的问题

# 软件服务器
- 由于漫画源经常失效，准备搭建Cimoc专用的服务器通过云端更新漫画源
- 未来会加入云备份功能，分享个人收藏漫画，导入他人分享收藏漫画功能
- 如果有大佬愿意赞助服务器的，会取消软件内的预置广告。

# 关于淘宝售卖和会员破解
- 请二次开发软件遵守软件许可证。
- 本程序没有任何破解网站VIP的功能，仅仅作为网页浏览器显示网站免费浏览部分，淘宝卖家自行添加的破解或其他功能与本程序无任何关系。

# 免责声明：
- 如果更新软件的过程中有什么侵权的地方，请在github上留言或者私信我，提供相关版权证明，会马上删除侵权部分的内容。
- 本软件开发，是为了码农之间的技术交流，以及解决软件测试过程中出现的各种bug，本软件的代码全部开源。
- 后期如进行到了服务器的开发，会开个新分支公开服务器开发所用的代码，以及公开服务器内存放的所有内容，
- 服务器存放的内容均来源于网站上的免费内容，如服务器内有侵权的内容会立马删除。
- 本软件不以盈利为目的，广告收入全部用于服务器维护和开发。
