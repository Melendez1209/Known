import router from '@ohos.router'

@Entry
@Component
struct Index {
  @State index: number = 0;
  private controller: TabsController = new TabsController();

  @Builder Tab(label: Resource, icon_normal: Resource, icon_fill: Resource, index: number) {
    Column() {
      Column() {
        Blank()
        Image(this.index == index ? icon_fill : icon_normal)
          .size({ width: 24, height: 24 })
          .fillColor(this.index == index ? "#264ff7" : "#2e2e2f")
        Text(label).margin({ top: 4 }).fontSize(10).fontColor(this.index == index ? "#264ff7" : "#2e2e2f")
      }.height('100%').width("100%").onClick(() => {
        this.index = index;
        this.controller.changeIndex(this.index);
      })
    }
    .justifyContent(FlexAlign.Center)
    .width('100%')
    .height(56)
  }

  build() {
    Column() {
      Tabs({ barPosition: BarPosition.End, controller: this.controller }) {
        TabContent() {
          Column() {
            Text($r("app.string.Tabs_home")).fontSize(30)
          }.width('100%').height('100%')
        }
        .tabBar(this.Tab($r("app.string.Tabs_home"), $r("app.media.ic_public_home"), $r("app.media.ic_public_home_filled"), 0))

        TabContent() {
          Column() {
            Search().width('90%')
          }.width('100%').height('100%')
        }
        .tabBar(this.Tab($r("app.string.Tabs_history"), $r("app.media.ic_public_timer"), $r("app.media.ic_public_timer_filled"), 1))

        TabContent() {
          Column() {
            Image($r("app.media.ic_user_portrait"))
              .width(78)
              .height(78)
              .objectFit(ImageFit.Contain).margin({ top: 8 })
            Row() {
              Row() {
                Image($r('app.media.ic_public_settings')).height(36).objectFit(ImageFit.Contain).margin({ left: 8 })
                Text($r('app.string.Settings_settings')).fontSize(20).margin({ left: 4 })
              }

              Image($r('app.media.ic_public_arrow_right')).height(22).objectFit(ImageFit.Contain).margin({ right: 8 })
            }
            .justifyContent(FlexAlign.SpaceBetween)
            .width('100%')
            .height(44)
            .margin({ top: 8 })
            .onClick(() => {
              router.pushUrl({ url: 'pages/Settings/Settings' })
            })
          }.width('100%').height('100%')
        }
        .tabBar(this.Tab($r("app.string.Tabs_me"), $r("app.media.ic_public_contacts"), $r("app.media.ic_public_contacts_filled"), 2))
      }
      .width('100%')
      .height('100%')
      .barHeight(60)
      .barMode(BarMode.Fixed)
      .onChange((index: number) => { // Page switch callback
        this.index = index;
      })
    }.width('100%').height('100%')
  }
}