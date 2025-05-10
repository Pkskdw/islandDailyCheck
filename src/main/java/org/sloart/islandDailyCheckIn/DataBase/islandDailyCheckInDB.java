package org.sloart.islandDailyCheckIn.DataBase;

import org.sloart.islandDailyCheckIn.main.islandDailyCheckInMain;

import java.io.File;
import java.sql.*;
import java.time.LocalDate;
import java.util.UUID;

public class islandDailyCheckInDB {

    private Connection connection;
    private final islandDailyCheckInMain plugin;

    public islandDailyCheckInDB(islandDailyCheckInMain plugin) {
        this.plugin = plugin;
        if (!connect(plugin.getDataFolder())) {
            plugin.getLogger().severe("데이터베이스 연결 실패! 플러그인 기능이 제한될 수 있습니다.");
        }
    }

    public boolean connect(File pluginFolder) {
        try {
            if (!pluginFolder.exists()) {
                pluginFolder.mkdirs();  // 폴더가 없으면 생성
            }

            File dbFile = new File(pluginFolder, "daily_checkin.db");
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());

            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate("CREATE TABLE IF NOT EXISTS login_records (" +
                        "uuid TEXT PRIMARY KEY," +
                        "reward_claimed TEXT" +
                        ")");
            }

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public synchronized String getRewardClaimedDate(UUID uuid) {
        if (connection == null) {
            plugin.getLogger().warning("데이터베이스 연결이 되지 않았습니다.");
            return null;
        }

        String query = "SELECT reward_claimed FROM login_records WHERE uuid = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, uuid.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("reward_claimed");
                } else {
                    insertLoginRecord(uuid);
                    return null;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public synchronized boolean hasClaimedToday(UUID uuid) {
        String currentDate = LocalDate.now().toString();
        String savedDate = getRewardClaimedDate(uuid);

        return savedDate != null && savedDate.equals(currentDate);
    }

    private void insertLoginRecord(UUID uuid) {
        if (connection == null) return;

        String insert = "INSERT INTO login_records (uuid, reward_claimed) VALUES (?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(insert)) {
            stmt.setString(1, uuid.toString());
            stmt.setNull(2, Types.VARCHAR);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            plugin.getLogger().warning("레코드 삽입 실패: " + uuid);
        }
    }

    public synchronized void setRewardClaimed(UUID uuid) {
        if (connection == null) return;

        String update = "UPDATE login_records SET reward_claimed = ? WHERE uuid = ?";
        try (PreparedStatement stmt = connection.prepareStatement(update)) {
            stmt.setString(1, LocalDate.now().toString());
            stmt.setString(2, uuid.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            plugin.getLogger().warning("보상 수령 처리 실패: " + uuid);
        }
    }

    public synchronized void resetRewardClaim(UUID uuid) {
        if (connection == null) return;

        String update = "UPDATE login_records SET reward_claimed = NULL WHERE uuid = ?";
        try (PreparedStatement stmt = connection.prepareStatement(update)) {
            stmt.setString(1, uuid.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            plugin.getLogger().warning("보상 초기화 실패: " + uuid);
        }
    }

    public void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            plugin.getLogger().warning("데이터베이스 연결 종료 실패: " + e.getMessage());
        }
    }
}
