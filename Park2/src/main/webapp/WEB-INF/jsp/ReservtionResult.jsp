<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>予約確認画面</title>
</head>
<body>
    <h1>異世界転生が完了しました。</h1>

    <p>電話番号: ${sessionScope.tel}</p>
    <p>車両番号: ${sessionScope.carNumber}</p>
    <p>チェックイン日: ${sessionScope.checkInDate}</p>
    <p>チェックアウト日: ${sessionScope.checkOutDate}</p>
    
    <form action="Return" method="post">
    	<input type="submit" name="button2" value="メインメニューへ戻る">
	</form>
</body>
</html>
