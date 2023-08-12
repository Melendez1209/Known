import router from '@ohos.router';

@Component
export default struct NavigationTitle {
  titleParam: Resource;

  build() {
    Row() {
      Image($r('app.media.ic_public_back'))
        .width(24)
        .height(24)
        .margin({ left: 24, right: 16 })
        .onClick(() => {
          router.back();
        })
      Text(this.titleParam)
        .fontColor("#FF000000")
        .fontSize(20)
    }
    .width('100%')
    .height(56)
  }
}