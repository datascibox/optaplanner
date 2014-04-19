/*
 * Copyright 2010 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaplanner.examples.manners2009.swingui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.optaplanner.core.impl.solution.Solution;
import org.optaplanner.examples.common.swingui.SolutionPanel;
import org.optaplanner.examples.common.swingui.TangoColorFactory;
import org.optaplanner.examples.manners2009.domain.Gender;
import org.optaplanner.examples.manners2009.domain.Guest;
import org.optaplanner.examples.manners2009.domain.Hobby;
import org.optaplanner.examples.manners2009.domain.HobbyPractician;
import org.optaplanner.examples.manners2009.domain.Manners2009;
import org.optaplanner.examples.manners2009.domain.Seat;
import org.optaplanner.examples.manners2009.domain.SeatDesignation;
import org.optaplanner.examples.manners2009.domain.Table;

public class Manners2009Panel extends SolutionPanel {

    public static final int MALE_FEMALE_ICON_VARIATION = 5;

    private GridLayout gridLayout;
    private Map<Hobby, ImageIcon> hobbyImageIconMap;
    private List<ImageIcon> maleImageIconList;
    private List<ImageIcon> femaleImageIconList;

    public Manners2009Panel() {
        gridLayout = new GridLayout(0, 1);
        setLayout(gridLayout);
        Hobby[] hobbies = Hobby.values();
        hobbyImageIconMap = new HashMap<Hobby, ImageIcon>(hobbies.length);
        for (Hobby hobby : hobbies) {
            String imageIconFilename;
            switch (hobby) {
                case TENNIS:
                    imageIconFilename = "hobbyTennis.png";
                    break;
                case GOLF:
                    imageIconFilename = "hobbyGolf.png";
                    break;
                case MOTORCYCLES:
                    imageIconFilename = "hobbyMotorcycles.png";
                    break;
                case CHESS:
                    imageIconFilename = "hobbyChess.png";
                    break;
                case POKER:
                    imageIconFilename = "hobbyPoker.png";
                    break;
                default:
                    throw new IllegalArgumentException("The hobby (" + hobby + ") is not supported.");
            }
            hobbyImageIconMap.put(hobby, new ImageIcon(getClass().getResource(imageIconFilename)));
        }
        maleImageIconList = new ArrayList<ImageIcon>(MALE_FEMALE_ICON_VARIATION);
        femaleImageIconList = new ArrayList<ImageIcon>(MALE_FEMALE_ICON_VARIATION);
        for (int i = 0; i < MALE_FEMALE_ICON_VARIATION; i++) {
             maleImageIconList.add(new ImageIcon(getClass().getResource("guestMale" + i + ".png")));
             femaleImageIconList.add(new ImageIcon(getClass().getResource("guestFemale" + i + ".png")));
        }
    }

    @Override
    public boolean isRefreshScreenDuringSolving() {
        return true;
    }

    private Manners2009 getManners2009() {
        return (Manners2009) solutionBusiness.getSolution();
    }

    public void resetPanel(Solution solution) {
        removeAll();
        Manners2009 manners2009 = (Manners2009) solution;
        TangoColorFactory tangoColorFactory = new TangoColorFactory();
        gridLayout.setColumns((int) Math.ceil(Math.sqrt(manners2009.getTableList().size())));
        Map<Seat, SeatPanel> seatPanelMap = new HashMap<Seat, SeatPanel>(manners2009.getSeatList().size());
        SeatPanel unassignedPanel = new SeatPanel(null);
        seatPanelMap.put(null, unassignedPanel);
        for (Table table : manners2009.getTableList()) {
            // Formula: 4(columns - 1) = tableSize
            int edgeLength = (int) Math.ceil(((double) (table.getSeatList().size() + 4)) / 4.0);
            JPanel tablePanel = new JPanel(new GridLayout(0, edgeLength));
            tablePanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createEmptyBorder(5, 5, 5, 5),
                    BorderFactory.createTitledBorder("Table " + table.getTableIndex())
            ));
            add(tablePanel);
            for (int y = 0; y < edgeLength; y++) {
                for (int x = 0; x < edgeLength; x++) {
                    int index;
                    if (y == 0) {
                        index = x;
                    } else if (x == (edgeLength - 1)) {
                        index = (edgeLength - 1) + y;
                    } else if (y == (edgeLength - 1)) {
                        index = 2 * (edgeLength - 1) + (edgeLength - 1 - x);
                    } else if (x == 0) {
                        index = 3 * (edgeLength - 1) + (edgeLength - 1 - y);
                    } else {
                        index = Integer.MAX_VALUE;
                    }
                    if (index < table.getSeatList().size()) {
                        Seat seat = table.getSeatList().get(index);
                        SeatPanel seatPanel = new SeatPanel(seat);
                        tablePanel.add(seatPanel);
                        seatPanelMap.put(seat, seatPanel);
                    } else {
                        tablePanel.add(new JPanel());
                    }
                }
            }
        }
        for (SeatDesignation seatDesignation : manners2009.getSeatDesignationList()) {
            SeatPanel seatPanel = seatPanelMap.get(seatDesignation.getSeat());
            seatPanel.setBackground(tangoColorFactory.pickColor(seatDesignation.getGuestJobType()));
            seatPanel.setSeatDesignation(seatDesignation);
        }
    }

    private class SeatPanel extends JPanel {

        public SeatPanel(Seat seat) {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.DARK_GRAY),
                    BorderFactory.createEmptyBorder(2, 2, 2, 2)));
            SeatDesignation dummySeatDesignation = new SeatDesignation();
            dummySeatDesignation.setGuest(null);
            dummySeatDesignation.setSeat(seat);
            setSeatDesignation(dummySeatDesignation);
        }

        public void setSeatDesignation(SeatDesignation seatDesignation) {
            removeAll();
            if (seatDesignation.getGuest() == null) {
                add(new JLabel("Empty seat"));
                return;
            }
            JButton button = new JButton(new SeatDesignationAction(seatDesignation));
            button.setAlignmentX(CENTER_ALIGNMENT);
            button.setMargin(new Insets(0, 0, 0, 0));
            add(button);
            JLabel jobTypeLabel = new JLabel(seatDesignation.getGuest().getJob().getJobType().getCode(), SwingConstants.CENTER);
            jobTypeLabel.setAlignmentX(CENTER_ALIGNMENT);
            add(jobTypeLabel);
            JLabel jobLabel = new JLabel(seatDesignation.getGuest().getJob().getName(), SwingConstants.CENTER);
            jobLabel.setAlignmentX(CENTER_ALIGNMENT);
            add(jobLabel);
            JPanel hobbyPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            hobbyPanel.setOpaque(false);
            hobbyPanel.setAlignmentX(CENTER_ALIGNMENT);
            for (HobbyPractician hobbyPractician : seatDesignation.getGuest().getHobbyPracticianList()) {
                Hobby hobby = hobbyPractician.getHobby();
                JLabel hobbyLabel = new JLabel(hobbyImageIconMap.get(hobby));
                hobbyLabel.setToolTipText(hobby.getLabel());
                hobbyPanel.add(hobbyLabel);
            }
            add(hobbyPanel);
        }

    }

    private ImageIcon determineGuestIcon(SeatDesignation seatDesignation) {
        Guest guest = seatDesignation.getGuest();
        if (guest == null) {
            return null;
        }
        List<ImageIcon> imageIconList = guest.getGender() == Gender.MALE ? maleImageIconList : femaleImageIconList;
        return imageIconList.get(guest.getId().intValue() % imageIconList.size());
    }

    private class SeatDesignationAction extends AbstractAction {

        private SeatDesignation seatDesignation;

        public SeatDesignationAction(SeatDesignation seatDesignation) {
            super(null, determineGuestIcon(seatDesignation));
            Seat seat = seatDesignation.getSeat();
            if (seat != null) {
                Guest guest = seatDesignation.getGuest();
                setToolTipText((guest == null ? "" : "Guest " + guest.getCode() + " @ ")
                                + "Seat " + seat.getSeatIndexInTable());
            }
            this.seatDesignation = seatDesignation;
        }

        public void actionPerformed(ActionEvent e) {
            List<SeatDesignation> seatDesignationList = getManners2009().getSeatDesignationList();
            JComboBox seatDesignationListField = new JComboBox(seatDesignationList.toArray());
            seatDesignationListField.setSelectedItem(seatDesignation);
            int result = JOptionPane.showConfirmDialog(Manners2009Panel.this.getRootPane(), seatDesignationListField,
                    "Select seat designation to switch with", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                SeatDesignation switchSeatDesignation = (SeatDesignation) seatDesignationListField.getSelectedItem();
                // TODO FIXME
                throw new UnsupportedOperationException();
//                solutionBusiness.doMove(new SwapMove(seatDesignation, switchSeatDesignation));
//                solverAndPersistenceFrame.resetScreen();
            }
        }

    }

}
