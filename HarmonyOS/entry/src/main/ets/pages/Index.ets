// 将类型定义移到类外部
interface ExamRecord {
  examName: string;
  time: string;
  score: number
}

@Entry
@Component
struct Index {
  @State currentIndex: number = 0;
  private tabsController: TabsController = new TabsController();
  // 使用定义好的接口
  private examRecords: ExamRecord[] = [
    { examName: 'Exam 1', time: '2024-03-20', score: 150 },
    { examName: 'Exam 2', time: '2024-03-19', score: 150 },
    { examName: 'Exam 3', time: '2024-03-18', score: 150 }
  ];

  @Builder
  TabBuilder(title: string, index: number, normalImage: Resource, selectedImage: Resource) {
    Column() {
      Image(this.currentIndex === index ? selectedImage : normalImage)
        .width(24)
        .height(24)
      Text(title)
        .fontColor(this.currentIndex === index ? '#1698CE' : '#66000000')
        .fontSize(10)
        .margin({ top: 4 })
    }
    .width('100%')
    .height(50)
    .justifyContent(FlexAlign.Center)
  }

  build() {
    Tabs({
      barPosition: BarPosition.End,
      controller: this.tabsController
    }) {
      // 首页标签页
      TabContent() {
        Column() {
          // 搜索栏
          Search({ placeholder: '搜索...' })
            .width('90%')
            .height(40)
            .backgroundColor('#1A000000')
            .margin({ top: 12 })

          // 考试信息展示区
          Grid() {
            GridItem() {
              Column() {
                Text('考试')
                  .fontSize(16)
                  .fontWeight(FontWeight.Medium)
                Text('Time')
                  .fontSize(14)
                  .fontColor($r('sys.color.mask_secondary'))
                Text('150')
                  .fontSize(24)
                  .fontWeight(FontWeight.Bold)
                  .margin({ top: 8 })
              }
              .width('100%')
              .height('100%')
              .backgroundColor($r('sys.color.comp_background_list_card'))
              .borderRadius(16)
              .padding(16)
            }

            // 可以添加更多GridItem...
          }
          .width('90%')
          .height(200)
          .margin({ top: 16 })
          .columnsTemplate('1fr 1fr')
          .rowsTemplate('1fr 1fr')
          .columnsGap(12)
          .rowsGap(12)
        }
        .width('100%')
        .height('100%')
        .backgroundColor('#F1F3F5')
      }
      .tabBar(this.TabBuilder('首页', 0, $r('app.media.home_normal'), $r('app.media.home_selected')))

      // 历史记录标签页
      TabContent() {
        Column() {
          List() {
            ForEach(this.examRecords, (item: ExamRecord) => {
              ListItem() {
                Row() {
                  Column() {
                    Text(item.examName)
                      .fontSize(16)
                      .fontWeight(FontWeight.Medium)
                    Text(item.time)
                      .fontSize(14)
                      .fontColor($r('sys.color.mask_secondary'))
                  }
                  .alignItems(HorizontalAlign.Start)

                  Text(item.score.toString())
                    .fontSize(20)
                    .fontWeight(FontWeight.Bold)
                }
                .width('90%')
                .height(72)
                .backgroundColor($r('sys.color.comp_background_list_card'))
                .borderRadius(16)
                .padding(16)
                .justifyContent(FlexAlign.SpaceBetween)
              }
              .margin({ top: 12 })
            }, (item: ExamRecord) => item.examName)
          }
          .width('100%')
        }
        .width('100%')
        .height('100%')
        .backgroundColor('#F1F3F5')
      }
      .tabBar(this.TabBuilder('历史', 1, $r('app.media.home_normal'), $r('app.media.home_selected')))

      // 个人中心标签页
      TabContent() {
        Column() {
          // 头像区域
          Image($r('app.media.home_selected'))
            .width(80)
            .height(80)
            .borderRadius(40)
            .margin({ top: 48, bottom: 24 })

          // 设置列表
          List() {
            ListItem() {
              SettingItem({ title: '设置', icon: $r('app.media.home_selected') })
            }

            ListItem() {
              SettingItem({ title: '关于', icon: $r('app.media.home_selected') })
            }
          }
          .width('100%')
          .margin({ top: 24 })
        }
        .width('100%')
        .height('100%')
        .backgroundColor('#F1F3F5')
      }
      .tabBar(this.TabBuilder('我的', 2, $r('app.media.home_normal'), $r('app.media.home_selected')))
    }
    .onChange((index: number) => {
      this.currentIndex = index;
    })
  }
}

@Component
struct SettingItem {
  @Prop title: string = ''; // 使用 @Prop 装饰器并提供默认值
  @Prop icon: Resource = $r('app.media.home_normal');

  build() {
    Row() {
      Row() {
        Image(this.icon)
          .width(24)
          .height(24)
        Text(this.title)
          .fontSize(16)
          .margin({ left: 12 })
      }

      Image($r('app.media.home_selected'))
        .width(24)
        .height(24)
    }
    .width('100%')
    .height(56)
    .padding({ left: 16, right: 16 })
    .backgroundColor($r('sys.color.comp_background_list_card'))
    .justifyContent(FlexAlign.SpaceBetween)
  }
}