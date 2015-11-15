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

device.type('Millbrae');
MonkeyRunner.sleep(pause)

# touch the screen
device.touch(260, 200, 'DOWN_AND_UP')
MonkeyRunner.sleep(pause)

# touch "to" edit text 		
device.touch(75, 155, 'DOWN_AND_UP')
MonkeyRunner.sleep(pause)

device.type('Embarcadero (SF)');
MonkeyRunner.sleep(pause)

# touch the screen and view results
device.touch(260, 200, 'DOWN_AND_UP')
MonkeyRunner.sleep(pause*5)

# click to reverse source and destination
device.touch(295, 125, 'DOWN_AND_UP')
MonkeyRunner.sleep(pause)

# click to reverse source and destination
device.touch(295, 125, 'DOWN_AND_UP')
MonkeyRunner.sleep(pause)

# view the details of the first option
device.touch(125, 290, 'DOWN_AND_UP')
MonkeyRunner.sleep(pause*2)

MonkeyRunner.sleep(2)

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

