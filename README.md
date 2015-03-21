Rise
====

### What is this?
Rise is an open source android application that functions as an alarm clock to
help you get out of bed in the morning. This is done by requiring you to scan
a picture of a smiley face before the alarm will turn off.

### Setup
- Build the android application and install the .apk on your phone.
- Print out or draw the image below somewhere that you will have to walk to
for the alarm to turn off.
- Set your alarm in the app to the times and days you want

### Sample Smiley
![Simely face](rise.png)

### How it works?
The smiley face is detected in a very simple fashion:
- Four lines are sent out from the center of the image in the cardinal directions
- The lines sent out E/W should never encounter a line in the scanned image
- The line sent out N should intersect 1 line and the line sent out S should intersect 2.

Therefore for best results, be sure to scan the center of the face.
