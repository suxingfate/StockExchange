SAMPLE

  Simple IAF project - used to show the basic functionality of the
  Integration Adapter Framework using Java Plugins.

DESCRIPTION

   In this project, an Event Correlator is used to receive "Stock" events and 
   monitor each stock's trend within 5 mins. If one stock's price is increased by 2% 
   within 5 mins, then a alert event will be emitted by apama.
   
   An IAF adapter is used to convert the custom input event format 
   into Apama events and to convert the matches back to the custom format. 
   
   The application demonstrates the use of the "payload plugin" to extract 
   fields from the incoming events that were not explicitly mapped by the 
   adapter.
  

FILES

   README.txt            This file
   config.xml            IAF config file
   static.xml            IAF config file
   Stock.mon            The stock monitor
   src                   Java source code for the codec and transport.
   plugins.log           Logger's output from Java adapter.
   JStockAdapter.jar     This jar is used to run.


BUILDING THE SAMPLES

   To build, simply use eclipse to export a jar file with name JStockAdapter.jar

   A successful build will produce a jar file called JFileAdapter.jar

   
USING THE SAMPLES

   To ensure that the environment is configured correctly for Apama, all the 
   commands below should be executed from an Apama Command Prompt, or from a 
   shell or command prompt where the bin\apama_env script has been run (or on 
   Unix, sourced). 

   1. Start the Apama Correlator by running:

      > correlator
     
   2. cd to the Stock.mon directory, inject the monitor:
   
      > engine_inject Stock.mon

   3. Register an event receiver to write events to stdout:
   
      > engine_receive
        
   
   4. Then start the IAF adapter:
      
      > iaf config.xml

   5. Once the adapter has stopped, shut down the Event Correlator, adapter
      and engine_receive processes by typing Ctrl-C in their respective
      windows.     
      

SAMPLE OUTPUT

   Once there is an alert event emitted, it will be shown on the engine_receive command console,
   also the event will be received by our java code. But I didn't do much in java code. So there is
   just one log will be output to plugins.log file. In the future, 
   I will do some implementation with upstream alert event within java code.
   and the log(log "counter";) from Stock.mon will be printed on correlator command console
