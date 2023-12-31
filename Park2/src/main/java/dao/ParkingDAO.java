package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import model.Reservation;

public class ParkingDAO {

	private static final String DRIVER_NAME = "com.mysql.cj.jdbc.Driver";
	private static final String JDBC_URL = "jdbc:mysql://localhost:65534/parking?useSSL=false&characterEncoding=UTF-8&serverTimezone=JST";
	private static final String DB_USER = "root";
	private static final String DB_PASS = "pass";

	static {
		try {
			Class.forName(DRIVER_NAME);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("JDBCドライバーの読み込みに失敗しました。", e);
		}
	}

	// 新しい予約を作成するメソッド
    public void createReservation(String cuname, String tel, String carNumber, String ci, String co) {
        try (Connection connection = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASS)) {
            // 既存の顧客が存在するか確認
            int customerId = getCustomerIdByTel(tel, connection);

            if (customerId != -1) {
                // 既存の顧客がいる場合
                // 電話番号が一致する予約があるか確認
                int existingReservationId = getReservationIdByTelAndParkDate(tel, connection);

                if (existingReservationId != -1) {
                    // 電話番号が一致する予約がある場合、その予約を更新
                    updateReservation(existingReservationId, carNumber, ci, co, connection);
                } else {
                    // 電話番号が一致する予約がない場合、新しい予約を作成
                    insertNewReservation(customerId, carNumber, ci, co, connection);
                }
            } else {
                // 既存の顧客がいない場合、新しい顧客と予約を作成
                int newCustomerId = insertNewCustomer(cuname, tel, ci, co, connection);
                insertNewReservation(newCustomerId, carNumber, ci, co, connection);
            }
        } catch (SQLException e) {
            e.printStackTrace(); // 適切なエラーハンドリングを行ってください
        }
    }

    // 電話番号に対応する顧客IDを取得するメソッド
    private int getCustomerIdByTel(String tel, Connection connection) throws SQLException {
        int customerId = -1;
        String query = "SELECT cuid FROM customer WHERE tel = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, tel);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    customerId = resultSet.getInt("cuid");
                }
            }
        }
        return customerId;
    }

    // 電話番号とチェックイン日に対応する予約IDを取得するメソッド
    private int getReservationIdByTelAndParkDate(String tel, Connection connection) throws SQLException {
        int reservationId = -1;
        String query = "SELECT reserv_id FROM reservation WHERE cuid = (SELECT cuid FROM customer WHERE tel = ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, tel);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    reservationId = resultSet.getInt("reserv_id");
                }
            }
        }
        return reservationId;
    }

    // 予約を更新するメソッド
    private void updateReservation(int reservationId, String carNumber, String checkInDate, String checkOutDate, Connection connection) throws SQLException {
        String query = "UPDATE reservation SET carnum = ?, pi = ? WHERE reserv_id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, carNumber);
            preparedStatement.setString(2, checkInDate);
            preparedStatement.setInt(3, reservationId);
            preparedStatement.executeUpdate();
        }
    }

    // 新しい顧客を作成し、その顧客IDを返すメソッド
    private int insertNewCustomer(String cuname, String tel, String ci, String co, Connection connection) throws SQLException {
        String insertCustomerQuery = "INSERT INTO customer (cuname, tel, ci, co) VALUES (?, ?, ?, ?)";
        try (PreparedStatement customerStatement = connection.prepareStatement(insertCustomerQuery, PreparedStatement.RETURN_GENERATED_KEYS)) {
            customerStatement.setString(1, cuname);
            customerStatement.setString(2, tel);
            customerStatement.setString(3, ci);
            customerStatement.setString(4, co);
            customerStatement.executeUpdate();

            try (ResultSet generatedKeys = customerStatement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Creating customer failed, no ID obtained.");
                }
            }
        }
    }


    // 新しい予約を作成するメソッド
    private void insertNewReservation(int customerId, String carNumber, String pi, String po, Connection connection) throws SQLException {
        String insertReservationQuery = "INSERT INTO reservation (carnum, cuid, pi, po) VALUES (?, ?, ?, ?)";
        try (PreparedStatement reservationStatement = connection.prepareStatement(insertReservationQuery)) {
            reservationStatement.setString(1, carNumber);
            reservationStatement.setInt(2, customerId);
            reservationStatement.setString(3, pi);
            reservationStatement.setString(4, po);
            reservationStatement.executeUpdate();
        }
    }

	public List<Reservation> getAllReservations() {
		List<Reservation> reservations = new ArrayList<>();
		String query = "SELECT * FROM reservation";

		try (Connection connection = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASS);
				PreparedStatement preparedStatement = connection.prepareStatement(query);
				ResultSet resultSet = preparedStatement.executeQuery()) {

			while (resultSet.next()) {
				int reserv_id = resultSet.getInt("reserv_id");
				String carnum = resultSet.getString("carnum");
				int cuid = resultSet.getInt("cuid");
				String pi = resultSet.getString("pi");

				String cuname = getCustomerNameById(cuid, connection); // 顧客名を取得

				// 予約オブジェクトを生成し、顧客名を設定
				Reservation reservation = new Reservation(reserv_id, carnum, cuid, cuname, pi);
				reservations.add(reservation);
			}
		} catch (SQLException e) {
			e.printStackTrace(); // 適切なエラーハンドリングが必要です
		}

		return reservations;
	}

	// 顧客IDに基づいて顧客名を取得するメソッド
	private String getCustomerNameById(int cuid, Connection connection) {
		String cuname = "";
		String customerQuery = "SELECT cuname FROM customer WHERE cuid = ?";
		try (PreparedStatement customerStatement = connection.prepareStatement(customerQuery)) {
			customerStatement.setInt(1, cuid);
			ResultSet customerResult = customerStatement.executeQuery();

			if (customerResult.next()) {
				cuname = customerResult.getString("cuname");
			}
		} catch (SQLException e) {
			e.printStackTrace(); // 適切なエラーハンドリングが必要です
		}
		return cuname;
	} 


	public List<Reservation> searchByCarNum(String carnum) {
		List<Reservation> reservations = new ArrayList<>();
		try (Connection connection = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASS)) {
			String query = "SELECT * FROM reservation WHERE carnum = ?";
			try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
				preparedStatement.setString(1, carnum);

				try (ResultSet resultSet = preparedStatement.executeQuery()) {
					while (resultSet.next()) {
						int reserv_id = resultSet.getInt("reserv_id");
						String carNumber = resultSet.getString("carnum");
						int customerId = resultSet.getInt("cuid");
						String pi = resultSet.getString("pi");
						String customerName = getCustomerNameById(customerId, connection); // 顧客名を取得
						Reservation reservation = new Reservation(reserv_id, carNumber, customerId, customerName, pi);
						reservations.add(reservation);
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace(); // 適切なエラーハンドリングを行ってください
		}
		return reservations;
	}

	public List<Reservation> searchByParkdate(String parkdate) {
		List<Reservation> reservations = new ArrayList<>();
		try (Connection connection = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASS)) {
			String query = "SELECT * FROM reservation WHERE pi = ?";
			try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
				preparedStatement.setString(1, parkdate);

				try (ResultSet resultSet = preparedStatement.executeQuery()) {
					while (resultSet.next()) {
						int reserv_id = resultSet.getInt("reserv_id");
						String carNumber = resultSet.getString("carnum");
						int customerId = resultSet.getInt("cuid");
						String pi = resultSet.getString("pi");
						String customerName = getCustomerNameById(customerId, connection); // 顧客名を取得
						Reservation reservation = new Reservation(reserv_id, carNumber, customerId, customerName, pi);
						reservations.add(reservation);
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace(); // 適切なエラーハンドリングを行ってください
		}
		return reservations;
	}

	public List<Reservation> searchByName(String cuname) {
		List<Reservation> reservations = new ArrayList<>();
		try (Connection connection = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASS)) {
			String query = "SELECT reservation.*, customer.cuname FROM reservation JOIN customer ON reservation.cuid = customer.cuid WHERE customer.cuname = ?;";
			try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
				preparedStatement.setString(1, cuname);

				try (ResultSet resultSet = preparedStatement.executeQuery()) {
					while (resultSet.next()) {
						int reserv_id = resultSet.getInt("reserv_id");
						String carNumber = resultSet.getString("carnum");
						int customerId = resultSet.getInt("cuid");
						String pi = resultSet.getString("pi");
						Reservation reservation = new Reservation(reserv_id,carNumber,customerId,pi,cuname);
						reservations.add(reservation);
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace(); // 適切なエラーハンドリングを行ってください
		}
		return reservations;
	}
	private String getAddressByTel(String tel, Connection connection) throws SQLException {
        String address = "";
        String query = "SELECT address FROM customer WHERE tel = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, tel);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    address = resultSet.getString("address");
                }
            }
        }
        return address;
    }
	
	public void deleteReservation(int reservationId) {
	    try (Connection connection = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASS)) {
	        String query = "DELETE FROM reservation WHERE reserv_id = ?";
	        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
	            preparedStatement.setInt(1, reservationId);

	            // DELETE文にはexecuteUpdate()を使用
	            int rowsAffected = preparedStatement.executeUpdate();

	            // 削除が成功したかどうかを確認
	            if (rowsAffected > 0) {
	                System.out.println("予約が正常に削除されました。");
	            } else {
	                System.out.println("IDが" + reservationId + "の予約は見つかりませんでした。");
	            }
	        }
	    } catch (SQLException e) {
	        e.printStackTrace(); // アプリケーション内で適切に例外を処理してください
	    }
	}
	
	public void editReservation(int reservationId, String carNumber, String parkDate) {
        try (Connection connection = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASS)) {
            String query = "UPDATE reservation SET carnum = ?, pi = ? WHERE reserv_id = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, carNumber);
                preparedStatement.setString(2, parkDate);
                preparedStatement.setInt(3, reservationId);

                int rowsAffected = preparedStatement.executeUpdate();

                if (rowsAffected > 0) {
                    System.out.println("予約が正常に更新されました。");
                } else {
                    System.out.println("IDが" + reservationId + "の予約は見つかりませんでした。");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // 適切なエラーハンドリングを行ってください
        }
    }

    // 予約IDに基づいて予約情報を取得するメソッド
    private Reservation getReservationById(int reservationId, Connection connection) throws SQLException {
        Reservation reservation = null;
        String query = "SELECT * FROM reservation WHERE reserv_id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, reservationId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    int customerId = resultSet.getInt("cuid");
                    String carNumber = resultSet.getString("carnum");
                    String parkDate = resultSet.getString("pi");

                    // 顧客名を取得
                    String customerName = getCustomerNameById(customerId, connection);

                    // 予約オブジェクトを生成
                    reservation = new Reservation(reservationId, carNumber, customerId, customerName, parkDate);
                }
            }
        }
        return reservation;
    }

    // 予約情報を更新するメソッド
    private void updateReservation(Reservation reservation, Connection connection) throws SQLException {
        String query = "UPDATE reservation SET carnum = ?, pi = ? WHERE reserv_id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, reservation.getCarnum());
            preparedStatement.setString(2, reservation.getParkdate());
            preparedStatement.setInt(3, reservation.getReserv_id());
            preparedStatement.executeUpdate();
        }
    }
}








