# Guidelines for using code formatting configuration files

## For IntelliJ
- First you should find a file present in the same folder named as `intellij-java-codestyle.xml` which contains the Java code style for IntelliJ.
- Navigate to the Settings present under `File` menu or just click `Ctrl+Alt+S`.
- Under settings, go to `Editor => Code Style => Java`.
- Click on `Scheme settings => Import Scheme => Intellij IDEA code style xml`, and then select the file by browsing from the dev folder and click Apply.
- Now, format the code of any file by simply pressing `Ctrl+Alt+L`.

## For Eclipse (Also NetBeans developers can refer this)
- First you should find a file present in the same folder named as `eclipse-java-codestyle.xml` which contains the Java code style for Eclipse.
- Navigate to the `Preferences` present under `Window` menu.
- Go to `Java => Code Style => Formatter`.
- Click on `Import` and browse the config file from the dev folder and just click `Apply and Close`.
- Now, format the code of any file by simply pressing `Ctrl+Shift+F`.

The code styles are from the [Google Style Guides](https://google.github.io/styleguide/) that contains formatting config files for different languages. For more information, visit the [google/styleguide](https://github.com/google/styleguide) repository.
