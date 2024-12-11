import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FootballLeagueGUI {
    private JFrame frame;
    private JPanel panel;
    private JLabel titleLabel;
    private JTable table;
    private DefaultTableModel tableModel;
    private JButton simulateButton;
    private JButton submitButton;
    private List<Fixture> fixtures;
    private int fixtureIndex;
    private League league;

    public FootballLeagueGUI() {
        frame = new JFrame("Football League");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        panel = new JPanel();
        panel.setLayout(new BorderLayout());

        titleLabel = new JLabel("League Standings");
        panel.add(titleLabel, BorderLayout.NORTH);

        tableModel = new DefaultTableModel();
        tableModel.addColumn("Team");
        tableModel.addColumn("Points");
        tableModel.addColumn("Wins");
        tableModel.addColumn("Draws");
        tableModel.addColumn("Losses");
        tableModel.addColumn("GF");
        tableModel.addColumn("GA");
        tableModel.addColumn("GD");

        table = new JTable(tableModel);
        table.setPreferredScrollableViewportSize(new Dimension(500, 200));
        table.setFillsViewportHeight(true);

        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);

        simulateButton = new JButton("Simulate Matches");
        submitButton = new JButton("Submit");

        simulateButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                fixtures = league.getFixtures();
                fixtureIndex = 0;

                if (fixtures.size() > 0) {
                    showFixture();
                    simulateButton.setEnabled(false);
                }
            }
        });

        submitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (fixtures != null && fixtureIndex < fixtures.size()) {
                    Fixture fixture = fixtures.get(fixtureIndex);
                    int homeGoals = Integer.parseInt(javax.swing.JOptionPane.showInputDialog("Enter the number of goals for the home team: "));
                    int awayGoals = Integer.parseInt(javax.swing.JOptionPane.showInputDialog("Enter the number of goals for the away team: "));

                    league.simulateMatch(fixture, homeGoals, awayGoals);
                    fixtureIndex++;

                    if (fixtureIndex < fixtures.size()) {
                        showFixture();
                    } else {
                        submitButton.setEnabled(false);
                        simulateButton.setEnabled(true);
                        JOptionPane.showMessageDialog(frame, "All matches have been simulated and submitted.");
                    }

                    updateTable();
                }
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(simulateButton);
        buttonPanel.add(submitButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        frame.getContentPane().add(panel);
        frame.pack();
        frame.setVisible(true);
    }

    public void initializeLeague() {
        int numTeams = Integer.parseInt(javax.swing.JOptionPane.showInputDialog("Enter the number of teams in the league: "));
        league = new League(numTeams);

        for (int i = 0; i < numTeams; i++) {
            String teamName = javax.swing.JOptionPane.showInputDialog("Enter the name of team " + (i + 1) + ": ");
            Team team = new Team(teamName);
            league.addTeam(team);
        }

        league.generateFixtures();
        updateTable();
    }

    private void showFixture() {
        Fixture fixture = fixtures.get(fixtureIndex);
        String homeTeamName = fixture.getHomeTeam().getName();
        String awayTeamName = fixture.getAwayTeam().getName();

        JOptionPane.showMessageDialog(frame, "Fixture: " + homeTeamName + " vs " + awayTeamName);
    }

    private void updateTable() {
        tableModel.setRowCount(0);

        List<Team> teams = league.getTeams();

        for (Team team : teams) {
            Object[] rowData = {
                    team.getName(),
                    team.getPoints(),
                    team.getWins(),
                    team.getDraws(),
                    team.getLosses(),
                    team.getGoalsFor(),
                    team.getGoalsAgainst(),
                    team.getGoalDifference()
            };

            tableModel.addRow(rowData);
        }
    }

    public static void main(String[] args) {
        FootballLeagueGUI leagueGUI = new FootballLeagueGUI();
        leagueGUI.initializeLeague();
    }

    class League {
        private List<Team> teams;
        private List<Fixture> fixtures;
        private int numTeams;

        public League(int numTeams) {
            this.numTeams = numTeams;
            teams = new ArrayList<>();
            fixtures = new ArrayList<>();
        }

        public void addTeam(Team team) {
            teams.add(team);
        }

        public List<Fixture> getFixtures() {
            return fixtures;
        }

        public void generateFixtures() {
            if (teams.size() != numTeams) {
                System.out.println("Not all teams have been added to the league.");
                return;
            }

            Collections.shuffle(teams);

            for (int i = 0; i < numTeams; i++) {
                Team homeTeam = teams.get(i);
                Team awayTeam;

                for (int j = 0; j < numTeams - 1; j++) {
                    int awayIndex = (i + j + 1) % numTeams;
                    awayTeam = teams.get(awayIndex);

                    Fixture fixture = new Fixture(homeTeam, awayTeam);
                    fixtures.add(fixture);
                }
            }
        }

        public void simulateMatch(Fixture fixture, int homeGoals, int awayGoals) {
            Team homeTeam = fixture.getHomeTeam();
            Team awayTeam = fixture.getAwayTeam();

            homeTeam.incrementGoalsFor(homeGoals);
            homeTeam.incrementGoalsAgainst(awayGoals);

            awayTeam.incrementGoalsFor(awayGoals);
            awayTeam.incrementGoalsAgainst(homeGoals);

            if (homeGoals > awayGoals) {
                homeTeam.incrementWins();
                awayTeam.incrementLosses();
            } else if (homeGoals < awayGoals) {
                homeTeam.incrementLosses();
                awayTeam.incrementWins();
            } else {
                homeTeam.incrementDraws();
                awayTeam.incrementDraws();
            }

            updateTeamStrengths();
        }

        private void updateTeamStrengths() {
            for (Team team : teams) {
                int totalGoals = team.getGoalsFor();
                int totalGoalsAgainst = team.getGoalsAgainst();
                int totalMatches = team.getWins() + team.getDraws() + team.getLosses();

                team.calculateStrengths(totalGoals, totalGoalsAgainst, totalMatches);
            }
        }

        public List<Team> getTeams() {
            return teams;
        }
    }

    class Team {
        private String name;
        private int wins;
        private int draws;
        private int losses;
        private int goalsFor;
        private int goalsAgainst;

        private double attackStrength;
        private double defenseStrength;

        public Team(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public int getPoints() {
            return wins * 3 + draws;
        }

        public int getGoalDifference() {
            return goalsFor - goalsAgainst;
        }

        public int getWins() {
            return wins;
        }

        public int getDraws() {
            return draws;
        }

        public int getLosses() {
            return losses;
        }

        public int getGoalsFor() {
            return goalsFor;
        }

        public int getGoalsAgainst() {
            return goalsAgainst;
        }

        public double getAttackStrength() {
            return attackStrength;
        }

        public double getDefenseStrength() {
            return defenseStrength;
        }

        public void calculateStrengths(int totalGoals, int totalGoalsAgainst, int totalMatches) {
            this.attackStrength = (double) totalGoals / totalMatches;
            this.defenseStrength = (double) totalGoalsAgainst / totalMatches;
        }

        public void incrementWins() {
            wins++;
        }

        public void incrementDraws() {
            draws++;
        }

        public void incrementLosses() {
            losses++;
        }

        public void incrementGoalsFor(int goals) {
            goalsFor += goals;
        }

        public void incrementGoalsAgainst(int goals) {
            goalsAgainst += goals;
        }
    }

    class Fixture {
        private Team homeTeam;
        private Team awayTeam;

        public Fixture(Team homeTeam, Team awayTeam) {
            this.homeTeam = homeTeam;
            this.awayTeam = awayTeam;
        }

        public Team getHomeTeam() {
            return homeTeam;
        }

        public Team getAwayTeam() {
            return awayTeam;
        }
    }
}
