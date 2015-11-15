from com.android.monkeyrunner import MonkeyRunner, MonkeyDevice

device = MonkeyRunner.waitForConnection()
pause = 0.4

# sets a variable with the package's internal name
package = 'pro.dbro.bart'

# sets a variable with the name of an Activity in the package
activity = 'pro.dbro.bart.TheActivity'

# sets the name of the component to start
runComponent = package + '/' + activity

# Runs the component
device.startActivity(component=runComponent)
MonkeyRunner.sleep(5)

# touch "from" edit text 		
device.touch(75, 100, 'DOWN_AND_UP')
MonkeyRunner.sleep(pause)

device.type('a');
MonkeyRunner.sleep(2)

# touch the first one
device.touch(110, 160, 'DOWN_AND_UP')
MonkeyRunner.sleep(1)

# touch "to" edit text 		
device.touch(75, 155, 'DOWN_AND_UP')
MonkeyRunner.sleep(pause)

device.type('b');
MonkeyRunner.sleep(2)

# touch the screen and view results
device.touch(110, 200, 'DOWN_AND_UP')
MonkeyRunner.sleep(4)

# read station details
device.touch(82, 288, 'DOWN_AND_UP')
MonkeyRunner.sleep(pause)


MonkeyRunner.sleep(1)

#press home and close the activity
device.press('KEYCODE_HOME', MonkeyDevice.DOWN_AND_UP)
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

