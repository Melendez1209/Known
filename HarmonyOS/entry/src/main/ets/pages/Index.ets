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
          .size({ width: 25, height: 25 })
          .fillColor(this.index == index ? "#264ff7" : "#2e2e2f")
        Text(label).fontSize(16).fontColor(this.index == index ? "#264ff7" : "#2e2e2f")
        Blank()
      }.height('100%').width("100%").onClick(() => {
        this.index = index;
        this.controller.changeIndex(this.index);
      })
    }
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
            Text($r("app.string.Tabs_me")).fontSize(30)
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