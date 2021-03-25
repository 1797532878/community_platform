$(function(){
	$("#sendBtn").click(send_letter);
	$(".close").click(delete_msg);
	$("#dleBtn1").click(delete_msg);
	$("#dleBtn2").click(delete_msg);
	$("#dleBtn3").click(delete_msg);
	$("#dleBtn4").click(delete_msg);
	$("#dleBtn5").click(delete_msg);
});

function send_letter() {
	$("#sendModal").modal("hide");

	var toName = $("#recipient-name").val();
	var content = $("#message-text").val();
	$.post(
		CONTEXT_PATH + "/letter/send",
		{"toName":toName,"content":content},
		function(data) {
			data = $.parseJSON(data);
			if(data.code == 0) {
				$("#hintBody").text("发送成功!");
			} else {
				$("#hintBody").text(data.msg);
			}

			$("#hintModal").modal("show");
			setTimeout(function(){
				$("#hintModal").modal("hide");
				location.reload();
			}, 2000);
		}
	);
}

function delete_msg() {
	var content = $(this).parent().next().text();
	var conversationId = $("#conversationId").val();
	$.post(
		CONTEXT_PATH + "/letter/delete",
		{"conversationId":conversationId,"content":content},
		function (data){
			data = $.parseJSON(data);

		}
	);
		// TODO 删除数据
		$(this).parents(".media").remove();
}

