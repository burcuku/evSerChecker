from com.android.monkeyrunner import MonkeyRunner, MonkeyDevice

device = MonkeyRunner.waitForConnection()
pause = 0.3

# sets a variable with the package's internal name
package = 'com.andrew.apollo'

# sets a variable with the name of an Activity in the package
activity = 'com.andrew.apollo.ui.activities.HomeActivity'

# sets the name of the component to start
runComponent = package + '/' + activity

# Runs the component
device.startActivity(component=runComponent)
MonkeyRunner.sleep(4)

# click on the play button
device.touch(235, 460, 'DOWN_AND_UP')
MonkeyRunner.sleep(3)

# move to playlist
device.touch(35, 90, 'DOWN_AND_UP')
MonkeyRunner.sleep(pause*5)

# move to the next song
device.touch(290, 435, 'DOWN_AND_UP')
MonkeyRunner.sleep(pause*5)

# move to playlist
device.touch(290, 90, 'DOWN_AND_UP')
MonkeyRunner.sleep(pause*5)

# move to recent
device.touch(290, 90, 'DOWN_AND_UP')
MonkeyRunner.sleep(pause*5)

# click to search
device.touch(235, 50, 'DOWN_AND_UP')
MonkeyRunner.sleep(pause)

device.type("mario")
MonkeyRunner.sleep(pause)
device.touch(160, 125, 'DOWN_AND_UP')
MonkeyRunner.sleep(1)

# click to search
device.touch(300, 450, 'DOWN_AND_UP')
MonkeyRunner.sleep(3)

# click to pause
device.touch(170, 150, 'DOWN_AND_UP')
MonkeyRunner.sleep(2)


#press home and close the activity
device.press('KEYCODE_HOME', MonkeyDevice.DOWN_AND_UP)
MonkeyRunner.sleep(pause*5)

MonkeyRunner.sleep(2)

# click to list recent apps
device.press('KEYCODE_APP_SWITCH', MonkeyDevice.DOWN_AND_UP)
MonkeyRunner.sleep(2)

# swipe the app out                                                                             
device.touch(60, 400, MonkeyDevice.DOWN)  

for i in range(1, 12):                                                              
    device.touch(60 + 20 * i, 400, MonkeyDevice.MOVE)                              
    MonkeyRunner.sleep(0.1)             
                                                             

# Remove finger from screen
device.touch(280, 400, MonkeyDevice.UP) 

MonkeyRunner.sleep(2) 

