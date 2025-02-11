package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import dto.QnABoardDTO;

public class QnABoardDAO {
	public QnABoardDAO() {
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private Connection getConnection() throws Exception {
		String url = "jdbc:oracle:thin:@localhost:1521:xe";
		String id = "kh";
		String pw = "kh";
		return DriverManager.getConnection(url, id, pw);
	}

	public int insert(QnABoardDTO dto) throws Exception {
		String sql = "insert into qnaboard values(qna_seq.nextval, ?,?,?,sysdate,default)";

		try (Connection con = this.getConnection(); PreparedStatement pstat = con.prepareStatement(sql)) {
			pstat.setString(1, dto.getTitle());
			pstat.setString(2, dto.getContents());
			pstat.setString(3, dto.getWriter());
			int result = pstat.executeUpdate();
			con.commit();
			return result;
		}
	}

	public List<QnABoardDTO> selectAll() throws Exception {
		String sql = "select * from qnaboard order by seq desc";

		try (Connection con = this.getConnection();
				PreparedStatement pstat = con.prepareStatement(sql);
				ResultSet rs = pstat.executeQuery();) {

			List<QnABoardDTO> list = new ArrayList<>();
			while (rs.next()) {
				QnABoardDTO dto = new QnABoardDTO();
				dto.setSeq(rs.getInt("seq"));
				dto.setTitle(rs.getString("title"));
				dto.setContents(rs.getString("contents"));
				dto.setWriter(rs.getString("writer"));
				dto.setWrite_date(rs.getTimestamp("write_date"));
				dto.setView_count(rs.getInt("view_count"));

				list.add(dto);
			}
			return list;
		}
	}

	public QnABoardDTO selectBySeq(int seq) throws Exception {
		String sql = "select * from qnaboard where seq=?";
		try (Connection con = this.getConnection(); PreparedStatement pstat = con.prepareStatement(sql);) {
			pstat.setInt(1, seq);
			try (ResultSet rs = pstat.executeQuery();) {
				rs.next();
				QnABoardDTO dto = new QnABoardDTO();
				dto.setSeq(rs.getInt("seq"));
				dto.setTitle(rs.getString("title"));
				dto.setContents(rs.getString("contents"));
				dto.setWriter(rs.getString("writer"));
				dto.setWrite_date(rs.getTimestamp("write_date"));
				dto.setView_count(rs.getInt("view_count"));
				return dto;
			}
		}
	}

	public int deleteBySeq(int seq) throws Exception {
		String sql = "delete from qnaboard where seq =?";
		try (Connection con = this.getConnection(); PreparedStatement pstat = con.prepareStatement(sql);) {
			pstat.setInt(1, seq);
			int result = pstat.executeUpdate();
			return result;
		}
	}

	public int updateBySeq(int seq, String title, String contents) throws Exception {
		String sql = "update qnaboard set title=?, contents=? where seq=?";
		try (Connection con = this.getConnection(); PreparedStatement pstat = con.prepareStatement(sql);) {
			pstat.setString(1, title);
			pstat.setString(2, contents);
			pstat.setInt(3, seq);
			int result = pstat.executeUpdate();
			con.commit();
			return result;
		}
	}

	// 조회수
		public int updateViewCount(int seq) throws Exception{
			String sql = "update qnaboard set view_count=view_count+1 where seq=?";

			try(Connection con = this.getConnection();
					PreparedStatement pstat = con.prepareStatement(sql);){
				pstat.setInt(1, seq);

				int result = pstat.executeUpdate();
				con.commit();
				return result;
			}
		}

	public int getRecordTotalCount() throws Exception {
		String sql = "select count(*) from qnaboard";
		try (Connection con = this.getConnection();
				PreparedStatement pstat = con.prepareStatement(sql);
				ResultSet rs = pstat.executeQuery();) {
			rs.next();
			return rs.getInt(1);
		}

	}

	public String getPageNavi(int currentPage) throws Exception {

		int recordTotalCount = this.getRecordTotalCount(); //144; // 총 데이터의 개수 -> 향후 실제 데이터베이스의 개수를 세어와야 함.

		int recordCountPerPage = 10; // 한 페이지에 몇개의 게시글을 보여 줄 건지
		int naviCountPerPage = 10; // 한 페이지에 몇개의 네비를 보여 줄 건지

		int pageTotalCount = 0; // 총 몇개의 페이지가 필요한가?

		if (recordTotalCount % recordCountPerPage > 0) { // 전체 페이지 + 1 해야 함.
			pageTotalCount = recordTotalCount / recordCountPerPage + 1;
		} else {
			pageTotalCount = recordTotalCount / recordCountPerPage;
		}

		if (currentPage < 1) {
			currentPage = 1;
		} else if (currentPage > pageTotalCount) {
			currentPage = pageTotalCount;
		}

		int startNavi = (currentPage - 1) / naviCountPerPage * naviCountPerPage + 1;
		int endNavi = startNavi + naviCountPerPage - 1;

		if (endNavi > pageTotalCount) {
			endNavi = pageTotalCount;
		}

		System.out.println("현재 페이지 : " + currentPage);
		System.out.println("네비 시작 값 : " + startNavi);
		System.out.println("네비 끝 값 : " + endNavi);

		boolean needNext = true;
		boolean needPrev = true;

		if (startNavi == 1) {
			needPrev = false;
		}

		if (endNavi == pageTotalCount) {
			needNext = false;
		}

		StringBuilder sb = new StringBuilder(); //문자열들을 모아줌 쌓아둠 객체 안으로 조립됨

		if (needPrev) {
			sb.append("<a href='list.qnaboard?cpage=" +(startNavi-1)+"'>< </a>");
		}

		for (int i = startNavi; i <= endNavi; i++) {
			if (currentPage == i) {
				sb.append("<a href=\"list.qnaboard?cpage=" + i + "\">[" + i + "] </a>");
			} else {
				sb.append("<a href=\"list.qnaboard?cpage=" + i + "\">" + i + " </a>");
			}
		}

		if (needNext) {
			sb.append("<a href='list.qnaboard?cpage= " + (endNavi + 1) + " '>> </a>");
		}
		return sb.toString();
	}
	

	// boradlist에서 보여지는 게시글 개수를 정하기 위한 메소드
		public List<QnABoardDTO> selectByPage(int cpage) throws Exception{

			// 게시글의 번호를 세팅한다.
			int start = cpage * 10 - 9;
			int end = cpage * 10;

			// 한 페이지에 게시글이 10개씩 보여지도록 하기 위해서 row_number를 활용하는데, 서브 쿼리를 활용해서 select 해준다.
			String sql = "select * from (select row_number() over(order by seq desc) line, qnaboard.* from qnaboard) where line between ? and ?";

			try(Connection con = this.getConnection();
					PreparedStatement pstat = con.prepareStatement(sql);){
				pstat.setInt(1, start);
				pstat.setInt(2, end);

				try(ResultSet rs = pstat.executeQuery();){
					List<QnABoardDTO> list = new ArrayList<QnABoardDTO>();

					while(rs.next()) {
						int seq = rs.getInt("seq");
						String title = rs.getString("title");
						String contents = rs.getString("contents");
						String writer = rs.getString("writer");
						Timestamp write_date = rs.getTimestamp("write_date");
						int view_count = rs.getInt("view_count");

						QnABoardDTO dto = new QnABoardDTO(seq, title, contents, writer, write_date, view_count);
						list.add(dto);
					}
					return list;
				}
			}
		}

//	public int insertDummy() throws Exception {
//		String sql = "select * from board values(board_seq.nextval, ?, ?, ?, susdate,default)";
//
//		Connection con = this.getConnection();
//
//		for (int i = 0; i < 144; i++) {
//			PreparedStatement pstat = con.prepareStatement(sql);
//			pstat.setString(1, "title : " +i);
//			pstat.setString(2, "contents : " +i);
//			pstat.setString(3, "writer : " +i);
//			pstat.executeUpdate();
//			con.commit();
//		}
//		con.close();
//		return 0 ;
//	}
//
//	
//
//	public static void main(String[] args) throws Exception {
//		BoardDAO dao = new BoardDAO();
//		dao.insertDummy();
//	}

}
