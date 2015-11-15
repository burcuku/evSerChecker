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

# move to the next song
device.touch(295, 455, 'DOWN_AND_UP')
MonkeyRunner.sleep(pause*5)

# move to the prev song
device.touch(185, 455, 'DOWN_AND_UP')
MonkeyRunner.sleep(pause*5)

# stop song
device.touch(235, 460, 'DOWN_AND_UP')
MonkeyRunner.sleep(pause*5)


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

