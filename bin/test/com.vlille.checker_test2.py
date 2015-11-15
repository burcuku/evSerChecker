from com.android.monkeyrunner import MonkeyRunner, MonkeyDevice

device = MonkeyRunner.waitForConnection()
pause = 0.4

# sets a variable with the package's internal name
package = 'com.vlille.checker'

# sets a variable with the name of an Activity in the package
activity = 'com.vlille.checker.ui.HomeActivity'

# sets the name of the component to start
runComponent = package + '/' + activity

# Runs the component
device.startActivity(component=runComponent)
MonkeyRunner.sleep(5)

# move to "all stations" tab
device.touch(160, 100, 'DOWN_AND_UP')
MonkeyRunner.sleep(pause*4)

# click on the third station's star
device.touch(20, 320, 'DOWN_AND_UP')
MonkeyRunner.sleep(pause)

# click on the third station's star
device.touch(20, 320, 'DOWN_AND_UP')
MonkeyRunner.sleep(pause)

# click on the second station's star
device.touch(20, 260, 'DOWN_AND_UP')
MonkeyRunner.sleep(pause)

# click on the second station's details
device.touch(210, 250, 'DOWN_AND_UP')
MonkeyRunner.sleep(pause)

# click on the route detail
device.touch(180, 310, 'DOWN_AND_UP')
MonkeyRunner.sleep(pause)

# click on the third's details
device.touch(210, 360, 'DOWN_AND_UP')
MonkeyRunner.sleep(pause)

MonkeyRunner.sleep(2)

#press home and close the activity
device.press('KEYCODE_HOME', MonkeyDevice.DOWN_AND_UP)
MonkeyRunner.sleep(pause*5)

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

