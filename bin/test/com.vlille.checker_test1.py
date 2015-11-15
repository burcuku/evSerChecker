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

# click on the third station's star (favorite it)
device.touch(23, 200, 'DOWN_AND_UP')
MonkeyRunner.sleep(pause*2)

# move to "favorite stations" tab
device.touch(55, 100, 'DOWN_AND_UP')
MonkeyRunner.sleep(pause)

# click on the first favorite station's star (unfavorite it)
device.touch(20, 165, 'DOWN_AND_UP')
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

