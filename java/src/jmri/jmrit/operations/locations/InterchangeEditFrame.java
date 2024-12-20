// InterchangeEditFrame.java
package jmri.jmrit.operations.locations;

import javax.swing.BorderFactory;

/**
 * Frame for user edit of a classification/interchange track. Adds two panels to
 * TrackEditFrame for train/route car drops and pulls.
 *
 * @author Dan Boudreau Copyright (C) 2008, 2011, 2012
 * @version $Revision$
 */
public class InterchangeEditFrame extends TrackEditFrame implements java.beans.PropertyChangeListener {

    /**
     *
     */
    private static final long serialVersionUID = 5077272536994978975L;

    public InterchangeEditFrame() {
        super();
    }

    public void initComponents(Location location, Track track) {
        _type = Track.INTERCHANGE;

        super.initComponents(location, track);

        _toolMenu.add(new IgnoreUsedTrackAction(this));
        _toolMenu.add(new TrackDestinationEditAction(this));
        _toolMenu.add(new ChangeTrackTypeAction(this));
        _toolMenu.add(new ShowTrainsServingLocationAction(Bundle.getMessage("MenuItemShowTrainsTrack"), _location, _track));
        _toolMenu.add(new ShowCarsByLocationAction(false, location.getName(), _trackName));
        addHelpMenu("package.jmri.jmrit.operations.Operations_Interchange", true); // NOI18N

        // override text strings for tracks
        // panelTrainDir.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("TrainInterchange")));
        paneCheckBoxes.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("TypesInterchange")));
        deleteTrackButton.setText(Bundle.getMessage("DeleteInterchange"));
        addTrackButton.setText(Bundle.getMessage("AddInterchange"));
        saveTrackButton.setText(Bundle.getMessage("SaveInterchange"));

        // finish
        pack();
        setVisible(true);
    }

//    private final static Logger log = LoggerFactory.getLogger(InterchangeEditFrame.class.getName());
}
