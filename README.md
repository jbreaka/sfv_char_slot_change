# sfv_char_slot_change
This tool is intended to swap mods to different character slots.  It requires <a href="https://www.oracle.com/technetwork/java/javase/downloads/jdk13-downloads-5672538.html">Java</a> to run.  This tool can run on any computer (Windows, Mac, Linux)

====We're in Alpha Currently=========<br>
Download latest version <a href="https://drive.google.com/open?id=13hqrrF7PLuSCSeNs8OAvHmsAPkkfj1jQ">here</a>.
To run open a command line/terminal and navigate to the directory of the sfv_char_slot_change JAR file. Type the following:

=======Example ================<br>
<ul>
<li>I have my mod RyuC1Awesome, which is Ryu the character for slot 1</li>
<li>I want to change it slot 2</li>
<li>I want the newly created mod to be named RyuC2Awesome.pak</li>
</ul>
By typing the following command I get the desired changes.<br><br>

java -Xmx4096m -jar sfv_char_slot_change-assembly-0.5.jar 2 RyuC1Awesome.pak RyuC2Awesome.pak

<br><i>note: In the command, I allocate 4G (4096M) of RAM.  You can allocate less, the entire PAK is loaded into memory and worked on in multiple copies.</i> 