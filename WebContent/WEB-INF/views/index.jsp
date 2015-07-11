<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
		<title>Insert title here</title>
	</head>
	
	<body>
		<% out.println("PEPE"); %>
		
		<form action="http://localhost:8080/ManguServer/ola" method="POST">
			<input type="text" name="name" />
			<input type="text" name="lastName" />
			<input type="text" name="age" />
			
			<input type="submit">
		</form>	
		
		<form id="file-form" action="archivo" method="post" enctype="multipart/form-data">
			Select File to Upload:<br>
			<input type="file" name="fileName">
			<input type="submit" value="Upload">
		</form>
		
		
	</body>
</html>