package muSync;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class Main {
	
	public static void main(String[] args) {
		//Establish connection
		Connect con = new Connect();

		//Creating initial window
		JFrame main_frame = new JFrame("muSync Login");
		main_frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        main_frame.setSize(400, 400);
                
        //Creating the Panel and adding to it to the JFrame
        JPanel main_panel = new JPanel();
        main_frame.add(main_panel);
        main_panel.setLayout(null);
        
        //Creating the title, stating whether the user is logging in or signing up
        JLabel title = new JLabel("Log In");
        title.setBounds(10, 10, 200, 25);
        main_panel.add(title);
        
        //Creating username and password text boxes
        JLabel user_label = new JLabel("Username:");
        user_label.setBounds(10, 50, 80, 25);
        main_panel.add(user_label);
        
        JTextField user_text = new JTextField();
        user_text.setBounds(100,50,165,25);
        main_panel.add(user_text);
        
        JLabel pass_label = new JLabel("Password:");
        pass_label.setBounds(10, 80, 80, 25);
        main_panel.add(pass_label);
        
        JTextField pass_text = new JTextField();
        pass_text.setBounds(100, 80, 165, 25);
        main_panel.add(pass_text);
        
        
        //This panel will contain the buttons for toggling logging in and creating an account.
        //Clicking on one button will make the other one appear and vice-versa.
        JPanel button_panel = new JPanel();
        
        JButton login_button = new JButton("Don't have an account? Click here to sign up.");
        JButton signup_button = new JButton("Already have an account? Click here to log in.");
        
        button_panel.add(login_button, Component.CENTER_ALIGNMENT);
        button_panel.add(signup_button, Component.CENTER_ALIGNMENT);
        signup_button.setVisible(false);
        
        login_button.addActionListener(new ActionListener() {
        	@Override
        	public void actionPerformed(ActionEvent arg0) {
        		title.setText("Sign Up");
        		login_button.setVisible(false);
        		signup_button.setVisible(true);
        	}
        });
        
        signup_button.addActionListener(new ActionListener() {
        	@Override
        	public void actionPerformed(ActionEvent arg0) {
        		title.setText("Log In");
        		signup_button.setVisible(false);
        		login_button.setVisible(true);
        	}
        });
        
        //Creating the confirm button
        JButton confirm_button = new JButton("Confirm");
        confirm_button.setBounds(10, 110, 255, 50);
        confirm_button.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				String user = user_text.getText();
				String pass = pass_text.getText();
				//Determines whether to perform a log in or sign up depending on the title.
				//I originally wanted to use a boolean for this purpose, but had visibility issues due
				//to this being an enclosed method.
				if (title.getText().equals("Log In")) {
					loginProcess(user, pass, main_frame);
				}
				else {
					signupProcess(user, pass);
				}
			}
        	
        });
        main_panel.add(confirm_button);
        
        main_frame.add(button_panel, BorderLayout.SOUTH);
        main_frame.setVisible(true);   
	}
	
	//Method for logging in
	//This method, along with signupProcess(), uses a table called 'accounts' which consists of two columns - username and password
	public static void loginProcess(String user, String pass, JFrame frame) {
		try {
			String stmt = "select username from accounts where (username=\'"+user+"\') and (password=\'"+pass+"\')";
			ResultSet rs = Connect.st.executeQuery(stmt);
			//if no results were fetched, there is no account for the entered credentials. Throw an exception.
			if (!rs.next()) {
				throw new Exception("Incorrect Credentials");
			}
			else {
				//Otherwise, the user's credentials are correct and access to the menu is granted.
				frame.setVisible(false);
				JOptionPane.showMessageDialog(null, "Successfully Logged In.");
				openMusyncMenu(user);
			}
		}
		catch (SQLException sqle) {
			JOptionPane.showMessageDialog(null, sqle);
		}
		catch (Exception e) {
			JOptionPane.showMessageDialog(null, e);
		}
	}
	
	//Method for registering a new account in the database.
	public static void signupProcess(String user, String pass) {
		try {
			String stmt = "select username from accounts where (username=\'"+user+"\')";
			ResultSet rs = Connect.st.executeQuery(stmt);
			//If the username already exists, throw an exception
			if (rs.next()) {
				throw new Exception("Username already exists.");
			}
			//if the password field is empty, or filled with white space, throw an exception
			if (pass==null || pass.trim().isEmpty()) {
				throw new Exception("Please enter a password.");
			}
			//Otherwise, add the credentials to the database.
			stmt = "insert into accounts(username,password) values (\'"+user+"\', \'"+pass+"\')";
			Connect.st.executeUpdate(stmt);
			JOptionPane.showMessageDialog(null, "Successfully Created Account.");
		} 
		catch(SQLException sqle) {
			JOptionPane.showMessageDialog(null, sqle);
		}
		catch(Exception e) {
			JOptionPane.showMessageDialog(null, e);
		}
	}

	//opens a window for accessing the windows for adding tracks, creating playlists, and viewing playlists
	public static void openMusyncMenu(String user) {
		//setting up the main window
		JFrame menu_frame = new JFrame("muSync Menu");
		menu_frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		menu_frame.setSize(500, 200);
		
		//setting up a welcome message at the top of the window
		JPanel title_panel = new JPanel();
		JLabel title_label = new JLabel("Welcome, "+user+"!");
		title_panel.add(title_label, Component.CENTER_ALIGNMENT);
		menu_frame.add(title_panel, BorderLayout.NORTH);
		
		//this menu_panel will hold three buttons, each of which will open a seperate window
		JPanel menu_panel = new JPanel();
		menu_panel.setBorder(new EmptyBorder(25,25,25,25));
		
		JButton add_tracks_button = new JButton("Add Tracks");
		add_tracks_button.setBounds(0,0,100,100);
		add_tracks_button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				openTrackMenu();
			}
		});
		JButton create_playlist_button = new JButton("Create/Edit Playlists");
		create_playlist_button.setBounds(125, 0, 100, 100);
		create_playlist_button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				openPlaylistMenu(user);
			}
		});
		JButton view_playlist_button = new JButton("View Saved Playlists");
		view_playlist_button.setBounds(250, 0, 100, 100);
		view_playlist_button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				openPlayerMenu(user);
			}
		});
		
		menu_panel.add(add_tracks_button);
		menu_panel.add(create_playlist_button);
		menu_panel.add(view_playlist_button);
		menu_frame.add(menu_panel, BorderLayout.CENTER);
	
		menu_frame.setVisible(true);
	}
	
	//This method is responsible for displaying the playlists belonging to the user, as well as the tracks in those playlists
	public static void openPlayerMenu(String user) {
		JFrame player_frame = new JFrame("Music Player");
		player_frame.setSize(800, 520);
		
		JPanel player_panel = new JPanel(null);
		
		//We will use a JList to display the playlists, and another JList to display the tracks in the selected playlist
		
		//We create a DefaultListModel, pl_names, for the JList, pl_list, to be based off of
		DefaultListModel<String> pl_names = new DefaultListModel<>();
		JList<String> pl_list = new JList<>(pl_names);
		pl_list.setBounds(10, 10, 150, 300);
		pl_list.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK), "Playlists:"));
		player_panel.add(pl_list);
		
		//We create a DefaultListModel, pl_tracks, for the JList, tracks_list, to be based off of
		DefaultListModel<String> pl_tracks = new DefaultListModel<>();
		JList<String> tracks_list = new JList<>(pl_tracks);
		tracks_list.setBounds(250, 10, 300, 450);
		tracks_list.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK), "Tracks in selected playlist:"));
		player_panel.add(tracks_list);
		
		//Here we use an SQL query to fetch all the playlists belonging to the user
		try {
			String sql = "select * from \"playlists\" where \"user\"=\'"+user+"\'";
			ResultSet rs = Connect.st.executeQuery(sql);
			while (rs.next()) {
				//Adding each name to pl_names will have each name displayed as an item in the JList, pl_list
				pl_names.addElement(rs.getString("name"));
			}
		} catch (SQLException e) {
			JOptionPane.showMessageDialog(null, e);
			e.printStackTrace();
		}
		
		//Here we add an ActionListneer to the JList, pl_list, so that every time the user clicks on a playlist,
		//The other JList, tracks_list, will populate with the names of all tracks in the selected playlist.
		pl_list.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent arg0) {
				//Start off by clearing any previously added tracks
				pl_tracks.clear();
				try {
					//We fetch the playlist that the user selected
					String sql = "select * from \"playlists\" where \"user\" =\'"+user+"\' and \"name\" =\'"+pl_list.getSelectedValue()+"\'";
					ResultSet rs = Connect.st.executeQuery(sql);
					rs.next();
					//Here, we fetch the SQL varchar array and convert it to a Java Array. This Array contians the URL of each track in the playlist.
					Array a = rs.getArray("tracks");
					//If the array is null, that means there are no tracks contained in the playlist. If this is the case the ActionListener ends here.
					if(a!=null) {
						//Convert the Array into a String[] Array
						String[] temp = (String[])a.getArray();
						//Here, we create an ArrayList, names, to store the names of each track
						ArrayList<String> names = new ArrayList<String>();
						for (String s : temp) {
							//Using the URL, we fetch the corresponding name using an SQL query
							ResultSet r = Connect.st.executeQuery("select * from \"tracks\" where \"URL\"=\'"+s+"\'");
							if (r.next())
								names.add(r.getString("name"));
						}
						//Once all the names are fetched, we add each one to pl_tracks to it will be displayed in the JList, tracks_list
						for (String s : names) {
							pl_tracks.addElement(s);
						}
					}
				} catch (SQLException e) {
					JOptionPane.showMessageDialog(null, e);
					e.printStackTrace();
				}
			}
		});
		
		//This play button will allow ther user to play the selected track in a new browser window
		JButton play_button = new JButton("Play");
		play_button.setBounds(600, 200, 100, 50);
		play_button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				//This if condition checks that the user has selected a track before continuing
				if(tracks_list.getSelectedIndex()!=-1) {
					Desktop desktop = java.awt.Desktop.getDesktop();
					try {
						String name = tracks_list.getSelectedValue();
						ResultSet rs = Connect.st.executeQuery("select * from \"tracks\" where \"name\"=\'"+name+"\'");
						rs.next();
						URI oURL = new URI(rs.getString("URL"));
						desktop.browse(oURL);
					} catch (URISyntaxException | IOException | SQLException e) {
					JOptionPane.showMessageDialog(null, e);
					}
				}
			}
		});
		player_panel.add(play_button);
		
		player_frame.add(player_panel);
		player_frame.setVisible(true);
	}

	//opens a window for adding and removing tracks to/from the database
	public static void openTrackMenu() {
		try {
			//Fetch all the tracks in the database
			String stmt = "select * from \"tracks\"";
			ResultSet rs = Connect.st.executeQuery(stmt);

			JFrame track_frame = new JFrame("Track Menu");
			track_frame.setSize(800,500);
		
			JPanel track_panel = new JPanel(null);
			
			//A DefaultListModel. trackList, is created to populate the JList, track_jlist.
			DefaultListModel<String> trackList = new DefaultListModel<>();
			//Populates trackList with each track from the SQL query
			while (rs.next())
				trackList.addElement(rs.getString("name")+" - "+rs.getString("artist"));
			
			JList<String> track_jlist = new JList<>(trackList);
			track_jlist.setBounds(10, 40, 480, 390);
			track_jlist.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK), "Tracks in the database"));
			track_panel.add(track_jlist);
			
			
			//The following JLabels and JTextFields for user input for names, artists, and URLs
			JLabel name_label = new JLabel("Name:");
			name_label.setBounds(500, 10, 100, 30);
			JLabel artist_label = new JLabel("Artist:");
			artist_label.setBounds(500, 60, 100, 30);
			JLabel url_label = new JLabel("YouTube URL:");
			url_label.setBounds(500, 110, 100, 30);
			track_panel.add(name_label);
			track_panel.add(artist_label);
			track_panel.add(url_label);
			
			JTextField name_text = new JTextField();
			name_text.setBounds(500, 40, 250, 30);
			JTextField artist_text = new JTextField();
			artist_text.setBounds(500, 90, 250, 30);
			JTextField url_text = new JTextField();
			url_text.setBounds(500, 140, 250, 30);
			track_panel.add(name_text);
			track_panel.add(artist_text);
			track_panel.add(url_text);
			
			//This text field is for the user to search for a track
			JTextField search_box = new JTextField();
			search_box.setBounds(10, 10, 250, 30);
			track_panel.add(search_box);
			
			//This DefaultListModel will be used to conatin search contents
			DefaultListModel<String> searchedList = new DefaultListModel<>();
			
			//This button is used to trigger a search
			JButton search_button = new JButton("Search");
			search_button.setBounds(260, 10, 115, 30);
			search_button.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					String in = search_box.getText();
					//checks that the search box contains something before proceeding
					if (!in.trim().isEmpty()) {
						//clears searchedList to clear a previous search
						searchedList.clear();
						//renames the title of the JList
						track_jlist.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK), "Tracks containing: "+in));
						//for every item in the trackList...
						for (int i = 0; i < trackList.size(); i++) {
							//...if a track contains the search...
							if (trackList.get(i).toLowerCase().contains(in.toLowerCase())) {
								//...add it to searchedList
								searchedList.addElement(trackList.get(i));
							}
						}
						//searchedList will now contain any track where the name or artist contains the search term
						//Now, we set the JList to use this searchedList
						track_jlist.setModel(searchedList);
					}
				}
			});
			track_panel.add(search_button);
			
			//This button is to clear the search box and reset the JList
			JButton clear_button = new JButton("Clear");
			clear_button.setBounds(375, 10, 115, 30);
			clear_button.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					//clear the searchedList
					searchedList.clear();
					//Restores original name to the JList
					track_jlist.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK), "Tracks in the database"));
					//set the JList to use to original trackList
					track_jlist.setModel(trackList);
				}
			});
			track_panel.add(clear_button);
			
			//This button is responsible for processing the user input and create a new track using it.
			JButton add_button = new JButton("Add Track");
			add_button.setBounds(500, 200, 100, 50);
			add_button.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					//checks that input is valid
					if (processText(name_text.getText(), artist_text.getText(), url_text.getText())) {
						try {
							//adding the track to the tracks table in the database
						String sql = "insert into \"tracks\"(\"name\",\"artist\",\"URL\") values "
								+ "(\'"+name_text.getText()+"\',\'"+artist_text.getText()+"\',\'"+url_text.getText()+"\')";
						Connect.st.executeUpdate(sql);
						//updating JList to display the added track
						trackList.clear();
						ResultSet rs = Connect.st.executeQuery("select * from \"tracks\"");
						while (rs.next())
							trackList.addElement(rs.getString("name")+" - "+rs.getString("artist"));
						JOptionPane.showMessageDialog(null, "Successfully added track.");
						} catch (SQLException sqle) {
							JOptionPane.showMessageDialog(null, sqle);
						}
					}
					
				}
				//Returns true if input text is formated correctly, false otherwise
				private boolean processText(String name, String artist, String url) {
					//checks is the name is empty
					if (name.isEmpty() || name.trim().isEmpty()) {
						JOptionPane.showMessageDialog(null, "Please enter a valid name.");
						return false;
					}
					//checks if the artist is empty
					if (artist.isEmpty() || artist.trim().isEmpty()) {
						JOptionPane.showMessageDialog(null, "Please enter a valid artist.");
						return false;
					}
					//checks that the url starts with 'https://www.youtube.com/watch?v='
					if (url.indexOf("https://www.youtube.com/watch?v=")!=0) {
						JOptionPane.showMessageDialog(null, "Please enter a valid YouTube URL.");
						return false;
					}
					//if all the checks are passed, the input is valid
					return true;
				}			
			});
			track_panel.add(add_button);
			
			//This button will allow the user to remove a selected track from the database
			JButton remove_button = new JButton("Remove Track");
			remove_button.setBounds(500, 300, 175, 50);
			remove_button.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					//checks if there is a selection in the JList
					if (track_jlist.getSelectedIndex()!=-1) {
						//opens a confirmation dialog window
						String item = track_jlist.getSelectedValue();
						int confirmation = JOptionPane.showConfirmDialog(null, "Are you sure you want the delete the following track:\n"+item, "Confirm Track Removal", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
						//if the user selects yes...
						if (confirmation == JOptionPane.YES_OPTION) {
							//...remove the track from the database...
							try {
							String sql = "delete from \"tracks\" where \"name\"=\'"+item.substring(0, item.indexOf("-")-1)+"\' and \"artist\"=\'"+item.substring(item.indexOf("-")+2)+"\'";
							int i = Connect.st.executeUpdate(sql);
							if (i>0)
								System.out.println("Successful Delete.");
							//...and reflect the removal in the JList
							trackList.clear();
							ResultSet rs = Connect.st.executeQuery("select * from \"tracks\"");
							while (rs.next())
								trackList.addElement(rs.getString("name")+" - "+rs.getString("artist"));
							JOptionPane.showMessageDialog(null, "Successfully removed track.");
							} catch (SQLException sqle) {
								sqle.printStackTrace();
							}
						}
					}
				}			
			});
			track_panel.add(remove_button);
			
			track_frame.add(track_panel);
			track_frame.setVisible(true);
		} catch(SQLException sqle) {
			JOptionPane.showMessageDialog(null, sqle);
		}
	}
	//opens a window for creating playlists using tracks in the database
	public static void openPlaylistMenu(String user) {
		//select_frame will be displayed to select a playlist to edit, or to create a new playlist.
		//create_frame appears if the user wants to create a new playlist
		JFrame select_frame = new JFrame("Playlist Selection");
		select_frame.setSize(500, 500);
		JFrame create_frame = new JFrame("Playlict Creator");
		create_frame.setSize(300, 200);
		
		
		//Creating JComponents
		JPanel select_panel = new JPanel(null);
		JPanel create_panel = new JPanel(null);
		
		//Setting up the window for creating playlists
		JLabel create_name = new JLabel("Name: ");
		create_name.setBounds(5, 5, 50, 30);
		create_panel.add(create_name);
		JTextField create_name_text = new JTextField();
		create_name_text.setBounds(100, 5, 150,30);
		create_panel.add(create_name_text);
		JButton create_confirm = new JButton("Confirm");
		create_confirm.setBounds(100, 50, 100, 50);
		create_confirm.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				String input = create_name_text.getText();
				//checks if the name isn't just white space
				if (input.trim().isEmpty()) {
					JOptionPane.showMessageDialog(null, "Please enter a name.");
				}
				else {
					try {
						//checks if the user already has playlist with the selected name
						ResultSet rs = Connect.st.executeQuery("select * from \"playlists\" where \"user\" =\'"+user+"\' and \"name\" =\'"+input+"\'");
						//if the query returns anything, a playlist already exists with the name
						if (rs.next())
							JOptionPane.showMessageDialog(null, "You already have a playlist with this name. Please enter another one.");
						else {
							//Otherwise, the playlist will be added to the database
							//It will contain the user's name, the playlist's name, as will an an id, which is simply a concatenation of the user's name and the playlist's name. 
							String sql = "insert into \"playlists\"(\"user\",\"name\",\"id\") values (\'"+user+"\',\'"+input+"\',\'"+user+input+"\')";
							Connect.st.executeUpdate(sql);
							create_frame.setVisible(false);
							//Now we will close the window and inform the user that the playlist was successfully created.
							select_frame.dispatchEvent(new WindowEvent(select_frame, WindowEvent.WINDOW_CLOSING));
							JOptionPane.showMessageDialog(null, "Successfully created playlist.");
							
						}
					} catch (SQLException e) {
						JOptionPane.showMessageDialog(null, e);
					}
				}
			}
		});
		create_panel.add(create_confirm);
		create_frame.add(create_panel);
		
		//Setting up the list of pre-existing playlists
		try {
			//fetch all playlists belonging to the user
			ResultSet rs = Connect.st.executeQuery("select * from \"playlists\" where \"user\"=\'"+user+"\'");
			//Populate a DefaultListModel, names, for the JList, pl_list, to be based on
			DefaultListModel<String> names = new DefaultListModel<>();
			while(rs.next()) {
				names.addElement(rs.getString("name"));
			}
			JList<String> pl_list = new JList<>(names);
			pl_list.setBounds(10, 10, 200, 240);
			select_panel.add(pl_list);
			
			//This button is responsible for opening the editing window using the selected playlist
			JButton select_button = new JButton("Confirm");
			select_button.setBounds(250, 50, 100, 40);
			select_button.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					//Will only function if an item in the JList is selected
					if (pl_list.getSelectedIndex()!=-1) {
						//closes the current window and opens the editing window
						select_frame.setVisible(false);
						openPlaylistEditor(user, pl_list.getSelectedValue());
					}
				}
			});
			select_panel.add(select_button);
			
			JButton select_remove = new JButton("Delete");
			select_remove.setBounds(250, 100, 100, 40);
			select_remove.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					//Will only function if an item in the JList is selected
					if (pl_list.getSelectedIndex()!=-1) {
						String item = pl_list.getSelectedValue();
						//opens a confirmation dialog window
						int confirmation = JOptionPane.showConfirmDialog(null, "Are you sure you want the delete the following playlist:\n"+item, "Confirm Playlist Removal", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
						//if the user selects yes...
						if (confirmation == JOptionPane.YES_OPTION) {
							//...remove the playlist from the database
							try {
								int i = Connect.st.executeUpdate("delete from \"playlists\" where \"id\"=\'"+user+item+"\'");
								if (i>0)
									System.out.println(item+" deleted");
								select_frame.dispatchEvent(new WindowEvent(select_frame, WindowEvent.WINDOW_CLOSING));
								JOptionPane.showMessageDialog(null, "Playlist Successfully Deleted");
							} catch (SQLException sqle) {
								sqle.printStackTrace();
							}
						}
					}
				}
			});
			select_frame.add(select_remove);
		} catch (SQLException e) {
			JOptionPane.showMessageDialog(null, e);
		}
		
		JLabel or_label = new JLabel("or");
		or_label.setBounds(240, 275, 30, 20);
		select_frame.add(or_label);
		
		//This button will open a new window to allow the user to make a new playlist
		JButton create_button = new JButton("Create Playlist");
		create_button.setBounds(250, 320, 200, 50);
		create_button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				create_frame.setVisible(true);
			}
		});
		select_frame.add(create_button);
		
		select_frame.add(select_panel);
		select_frame.setVisible(true);
	}

	public static void openPlaylistEditor(String user, String name) {
		//initialize JComponents
		JFrame edit_frame = new JFrame("Playlist Editor");
		edit_frame.setSize(1280, 720);
		JPanel edit_panel = new JPanel(null);
		JLabel selection_name = new JLabel("Name:");
		selection_name.setBounds(460, 320, 300, 30);
		edit_panel.add(selection_name);
		JLabel selection_artist = new JLabel("Artist:");
		selection_artist.setBounds(460, 360, 300, 30);
		edit_panel.add(selection_artist);
		JLabel selection_url = new JLabel("URL:");
		selection_url.setBounds(460, 400, 500, 30);
		edit_panel.add(selection_url);
		
		//We use a DefaultListModel, pl_tracks, for the JList, pl_track_jlist, to be based on. These will represent the tracks contained in the playlist.
		DefaultListModel<String> pl_tracks = new DefaultListModel<>();
		JList<String> pl_track_jlist = new JList<>(pl_tracks);
		pl_track_jlist.setBounds(10, 320, 450, 300);
		pl_track_jlist.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK), "Tracks in "+name+":"));
		try {
			//fetching the appropriate playlist
			String sql = "select * from \"playlists\" where \"user\" =\'"+user+"\' and \"name\" =\'"+name+"\'";
			ResultSet rs = Connect.st.executeQuery(sql);
			rs.next();
			//Storing the SQL varchar[] array as a Java Array
			Array a = rs.getArray("tracks");
			//if the array is empty, the playlist contains to tracks. If the array is not empty, we will continue
			if (a!=null) {
				//Convert the array into a String[] array
				String[] temp = (String[])a.getArray(); 
				for (String s : temp) {
					//For each URL, represented as String 's', we will fetch the corresponding name... 
					rs = Connect.st.executeQuery("select * from \"tracks\" where \"URL\"=\'"+s+"\'");
					if(rs.next())
						//...and add it to the DefaultListModel pl_tracks, which will in turn add it to the JList, pl_track_jlist
						pl_tracks.addElement(rs.getString("name"));
				}
			}
		} catch (SQLException e) {
			JOptionPane.showMessageDialog(null, e);
			e.printStackTrace();
		}
		//Every time a different track is selected, the information in the JLabels will change accordingly to display the track's name, artist, and URL.
		pl_track_jlist.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent arg0) {
				String name = pl_track_jlist.getSelectedValue();
				try {
					ResultSet rs = Connect.st.executeQuery("select * from \"tracks\" where \"name\"=\'"+name+"\'");
					rs.next();
					selection_name.setText("Name: "+name);
					selection_artist.setText("Artist: "+rs.getString("artist"));
					selection_url.setText("URL: "+rs.getString("URL"));
				} catch (SQLException e) {
					JOptionPane.showMessageDialog(null, e);
					e.printStackTrace();
				}
			}			
		});
		edit_panel.add(pl_track_jlist);
		
		JLabel db_selection_name = new JLabel("Name:");
		db_selection_name.setBounds(460, 150, 300, 30);
		edit_panel.add(db_selection_name);
		JLabel db_selection_artist = new JLabel("Artist:");
		db_selection_artist.setBounds(460, 190, 300, 30);
		edit_panel.add(db_selection_artist);
		JLabel db_selection_url = new JLabel("URL:");
		db_selection_url.setBounds(460, 230, 500, 30);
		edit_panel.add(db_selection_url);
		
		//Here we create a DefaultListModel, db_tracks, to populate the JList, db_track_jlist. These will represent all the tracks in the database.
		DefaultListModel<String> db_tracks = new DefaultListModel<>();
		JList<String> db_track_jlist = new JList<>(db_tracks);
		db_track_jlist.setBounds(10, 10, 450, 300);
		db_track_jlist.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK), "Tracks in the database:"));
		try {
			String sql = "select * from \"tracks\"";
			ResultSet rs = Connect.st.executeQuery(sql);
			while (rs.next()) {
				db_tracks.addElement(rs.getString("name"));
			}
		} catch (SQLException e) {
			JOptionPane.showMessageDialog(null, e);
			e.printStackTrace();
		}
		db_track_jlist.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent arg0) {
				String name = db_track_jlist.getSelectedValue();
				try {
					ResultSet rs = Connect.st.executeQuery("select * from \"tracks\" where \"name\"=\'"+name+"\'");
					if(rs.next()) {
						db_selection_name.setText("Name: "+name);
						db_selection_artist.setText("Artist: "+rs.getString("artist"));
						db_selection_url.setText("URL: "+rs.getString("URL"));
					}
				} catch (SQLException e) {
					JOptionPane.showMessageDialog(null, e);
					e.printStackTrace();
				}
			}
		});
		edit_panel.add(db_track_jlist);
		
		//This button is responsible for adding the selected track into the playlist
		JButton add_button = new JButton("Add Selected Track");
		add_button.setBounds(460, 280, 200, 30);
		add_button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				//checks that the user selected a track from the database
				if (db_track_jlist.getSelectedIndex()!=-1) {
					try {
					//checks that the selected track is not already in the playlist
					String selectedURL = db_selection_url.getText().substring(5);
					ResultSet rs = Connect.st.executeQuery("select * from \"playlists\" where \"user\" =\'"+user+"\' and \"name\" =\'"+name+"\'");
					rs.next();
					Array a = rs.getArray("tracks");
					if (a!=null ) {
						String[] temp = (String[])a.getArray();
						boolean isFound = false;
						for (String s : temp) {
							if (s.equals(selectedURL)) {
								isFound = true;
							}
						}
						if (isFound) {
							JOptionPane.showMessageDialog(null, "This track already exists in your playlist.");
						}
						else {
							//adds track to the playlist
							Connect.st.executeUpdate("update \"playlists\" set \"tracks\"=array_append(tracks, \'"+selectedURL+"\') where \"id\"=\'"+user+name+"\'");
							pl_tracks.addElement(db_track_jlist.getSelectedValue());
							JOptionPane.showMessageDialog(null, "Successfully added track to "+name);
						}
					}
					else {
						Connect.st.executeUpdate("update \"playlists\" set \"tracks\"=\'{"+selectedURL+"}\' where \"id\"=\'"+user+name+"\'");
						pl_tracks.addElement(db_track_jlist.getSelectedValue());
						JOptionPane.showMessageDialog(null, "Successfully added track to "+name);
					}
					} catch (SQLException e) {
						JOptionPane.showMessageDialog(null, e);
						e.printStackTrace();
					}
				}
			}
		});
		edit_panel.add(add_button);
		
		//This text field is for the user to search for a track
		JTextField search_box = new JTextField();
		search_box.setBounds(460, 10, 250, 30);
		edit_panel.add(search_box);
		
		//This DefaultListModel will be used to contain search contents
		DefaultListModel<String> searchedList = new DefaultListModel<>();
		
		//This button is used to trigger a search
		JButton search_button = new JButton("Search");
		search_button.setBounds(710, 10, 115, 30);
		search_button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				String in = search_box.getText();
				//checks that the search box contains something before proceeding
				if (!in.trim().isEmpty()) {
					//clears searchedList to clear a previous search
					searchedList.clear();
					//renames the title of the JList
					db_track_jlist.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK), "Tracks containing: "+in));
					//for every item in the trackList...
					for (int i = 0; i < db_tracks.size(); i++) {
						//...if a track contains the search...
						if (db_tracks.get(i).toLowerCase().contains(in.toLowerCase())) {
							//...add it to searchedList
							searchedList.addElement(db_tracks.get(i));
						}
					}
					//searchedList will now contain any track where the name or artist contains the search term
					//Now, we set the JList to use this searchedList
					db_track_jlist.setModel(searchedList);
				}
			}
		});
		edit_panel.add(search_button);
		
		//This button is to clear the search box and reset the JList
		JButton clear_button = new JButton("Clear");
		clear_button.setBounds(825, 10, 115, 30);
		clear_button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				//clear the searchedList
				searchedList.clear();
				//Restores original name to the JList
				db_track_jlist.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK), "Tracks in the database"));
				//set the JList to use to original trackList
				db_track_jlist.setModel(db_tracks);
			}
		});
		edit_panel.add(clear_button);
		
		edit_frame.add(edit_panel);
		edit_frame.setVisible(true);
	}
}