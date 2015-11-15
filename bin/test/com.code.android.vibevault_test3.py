from com.android.monkeyrunner import MonkeyRunner, MonkeyDevice

device = MonkeyRunner.waitForConnection()
pause = 0.4

# sets a variable with the package's internal name
package = 'com.code.android.vibevault'

# sets a variable with the name of an Activity in the package
activity = 'com.code.android.vibevault.HomeScreen'

# sets the name of the component to start
runComponent = package + '/' + activity

# Runs the component
device.startActivity(component=runComponent)
MonkeyRunner.sleep(3)

# click on downloads
device.touch(80, 320, 'DOWN_AND_UP')
MonkeyRunner.sleep(pause*3)

# click on the menu
device.touch(295, 180, 'DOWN_AND_UP')
MonkeyRunner.sleep(pause*3)

# click on refresh downloaded folder
device.touch(155, 100, 'DOWN_AND_UP')
MonkeyRunner.sleep(pause*3)

# click on the menu
device.touch(295, 180, 'DOWN_AND_UP')
MonkeyRunner.sleep(pause*3)

# click on refresh downloaded folder
device.touch(155, 100, 'DOWN_AND_UP')
MonkeyRunner.sleep(pause*3)

# go back to the main screen
device.touch(35, 50, 'DOWN_AND_UP')
MonkeyRunner.sleep(pause*5)


MonkeyRunner.sleep(3)

#press home and close the activity
device.press('KEYCODE_HOME', MonkeyDevice.DOWN_AND_UP)
MonkeyRunner.sleep(5)

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

MonkeyRunner.sleep(5) 

