# CONTRIBUTING

In this document, we will make requirements for code or other necessary content for contributors who
participate or want to participate in the development of this project.

## Objectives

The aim of this project is to provide **free** academic analysis and encouragement to students at
junior and senior school level through a number of factors, including grades. This means that we do
not charge for any of the features, but this does not mean that we do not provide users with access
to sponsorship, etc., for funding sources. On each platform, every effort is made to simplify the
development process and increase productivity by adopting the development model it officially
promotes.In addition to this, we respect the individual needs of each device manufacturer and user
and therefore do not make changes to the content, this includes fonts, pop-up styles, etc.

## The Bottom Line

We expressly forbid anyone from posting comments about pornography, violence, gambling, drugs, etc.
in any code or section! If found, we will deal with you seriously! In particular, the audience of
this software is mainly underage students.

## Android

### Prerequisites

For the Android version, we use Jetpack Compose, the official development model promoted by Google,
and the official Material Design 3 software design specification by Google. You are expected to
continue and adhere to them. We recommend that you meet the following conditions before
participating in our development:

1. Knowledge of Kotlin grammar
2. Compose (platform-independent) development mindset
3. Ability to use Gradle in Kotlin DLS mode

### Code Specification

Having fulfilled the above conditions, we are looking forward to working with you, which will make
our projects even better and more creative. However, we would like to draw your attention to the
following points:

1. Using **`CRLF`** as a line break
2. Leave a blank line at the end of each file.
3. Use the code cleanup tool that comes with Android Studio to format the files you have changed
   before committing.
4. If you are using multiple lines of code to pass parameters to a `Unit` etc., your brackets for
   these functions should not do anything on the first line and the writing closure brackets should
   be on the next line of your operation.

   ```kotlin
   /** This is what we promote **/
   Row(
       modifier = Modifier.fillMaxSize(),
       horizontalArrangement = Arrangement.SpaceBetween
   ) {
       // Your content 
   }
   // And
   Button(
          modifier = Modifier.align(Alignment.CenterHorizontally),
          onClick = {
              //TODO:Login
          }
      ) {
          Text(text = stringResource(R.string.sign_to))
      }
   
   /** This is what we do not promote **/
   Row(modifier = Modifier.fillMaxSize(),
       horizontalArrangement = Arrangement.SpaceBetween) {
       // Your content 
   } 
   // Or
   Button(
          modifier = Modifier.align(Alignment.CenterHorizontally),
          onClick = { 
              //TODO:Login
          }) {
           Text(text = stringResource(R.string.sign_to))
        }
   ```
5. You should comment your code appropriately, with a space before and after the comment symbol (no
   space before the symbol if the whole line is a comment) and without a full stop at the end of the
   comment (except for multi-line comments), during which time you need to use standard **English**
   (no restrictions on accents).
   ```kotlin
   // Variables related to settings
    var labelMode by rememberSaveable { mutableStateOf(false) } // Whether to display the home navigation bar labels
    var colorMode by rememberSaveable { mutableStateOf(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) }
    var grade by rememberSaveable { mutableIntStateOf(10) }

    // Variables related to components
    var visibleAppearance by remember { mutableStateOf(true) }
    var visibleAnalysis by remember { mutableStateOf(true) }
   ```

# That's all! We look forward to hearing from you!