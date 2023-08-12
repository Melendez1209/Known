import 'ets/view/Navigations'
import NavigationTitle from 'ets/view/Navigations'

@Entry
@Component
struct Settings {
  @Builder Title() {
    NavigationTitle({ titleParam: $r('app.string.Settings_settings') })
  }

  build() {
    NavDestination() {
      Column() {
        Row() {
          Text($r('app.string.Settings_appearance'))
        }.width('100%')
      }
    }.title(this.Title()).width('100%').height('100%')
  }
}