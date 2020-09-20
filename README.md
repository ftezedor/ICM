# ICM
Internet Connectivity Monitor

### Licence
GNU General Public License v2.0

### About
In order to up my skills regarding threads, I came up with this idea of developing an app whose unique purpose would be to monitor the connectivity to the internet.<br>
The first version came to life in two weeks or so but, from time to time, a new idea pops up in my head and I get back to this code.<br>
Thus, over the years, some features were added and improvements were made.<br>
Nevertheless, I believe more could be done and probably there are some bugs to be fixed.<br>
Nonetheless, I'd say it has already satisfied my initial intent so, that's it (for now).

### Dependencies
- Java 1.8.0_261 or later
- simple-xml-2.7.1.jar (for config deserialization)
- log4j 2.11.1 (core and api) for logging stout and stderr

### Usage
```
package br.com.tz.testing;

import br.com.tz.networking.ICCL;
import br.com.tz.networking.ICM;
import br.com.tz.networking.InternetConnectivityMonitor.ICMEvent;
import br.com.tz.networking.InternetConnectivityMonitor.ICMStatus;

public class ICMTester
{
	public static void main(String[] args) throws Exception
	{
		Tester t1 = new Tester();
		Tester t2 = new Tester();

		ICM.addConnectivityChangeListener(t1);
		ICM.addConnectivityChangeListener(t2);

		Thread.sleep(12000);

		ICM.removeConnectivityChangeListener(t1);

		Thread.sleep(1000);

		System.out.println("exiting main method");
	}
}

class Tester implements ICCL
{
	@Override
	public void onIcmStatusChange(ICMEvent evt, ICMStatus stt)
	{
		if ( evt == ICMEvent.CON_FAILURE ) return;
		System.out.println("Thread: " + Thread.currentThread().getName() + ", Event occurred: " + evt.name() + ", Connectivity status: " + stt.name());
	}
}
```
