package com.gauronit.tagmata;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.gauronit.tagmata.views.MainView;

import org.jdesktop.application.Application;
import org.jdesktop.application.SingleFrameApplication;

public class Main extends SingleFrameApplication {

    /**
     * At startup create and show the main frame of the application.
     */
    @Override protected void startup() {
    	//show(new MainView(this).getFrame());
    	try {
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
		} catch (ClassNotFoundException | InstantiationException
				| IllegalAccessException | UnsupportedLookAndFeelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	new MainView().setVisible(true);
    }

    /**
     * This method is to initialize the specified window by injecting resources.
     * Windows shown in our application come fully initialized from the GUI
     * builder, so this additional configuration is not needed.
     */
    @Override protected void configureWindow(java.awt.Window root) {
    }

    /**
     * A convenient static getter for the application instance.
     * @return the instance of Main
     */
    public static Main getApplication() {
        return Application.getInstance(Main.class);
    }

    /**
     * Main method launching the application.
     */
    public static void main(String[] args) {
        //launch(Main.class, args);
    	new Main().startup();
    }
}
