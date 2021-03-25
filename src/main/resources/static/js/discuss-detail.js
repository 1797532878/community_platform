//页面加载完成后
$(function (){
   $("#topBtn").click(setTop);
   $("#wonderfulBtn").click(setWonderful);
   $("#deleteBtn").click(setDelete);
});


//点赞
function like(btn,entityType,entityId,entityUserId,postId){
    $.post(
      CONTEXT_PATH + "/like",
        {"entityType":entityType,"entityId":entityId,"entityUserId":entityUserId,"postId":postId},
        function (data){
          data = $.parseJSON(data);
          if (data.code == 0){
              $(btn).children("i").text(data.likeCount);
              $(btn).children("b").text(data.likeStatus==1?"已赞":"赞");
          }else{
              alert(data.msg);
          }
        }
    );
}

// $(function(){
//     $("#submit_button_11").click(submit_11);
// });
//
// function submit_11(){
//     var content = $("#content_11").val();
//     var entityType = $("#entityType_11").val();
//     var entityId =$("#entityId_11").val();
//     var post_id = $("#post_id_11").val();
//     var targetId = $("#targetId_11").val();
//     $.post(
//         CONTEXT_PATH + "/comment/add/" + post_id,
//         {"content":content,"entityType":entityType,"entityId":entityId,"targetId":targetId,"post_id":post_id},
//         function (data){
//             data = $.parseJSON(data);
//             if (data.code == 0){
//                 window.location.reload();
//             }
//         }
//     );
// }

function comment(btn,entityType,entityId,post_id,targetId){
    var content = $(btn).parent().prev().children().val();
    $.post(
      CONTEXT_PATH + "/comment/add/" + post_id,
        {"content":content,"entityType":entityType,"entityId":entityId,"targetId":targetId,"post_id":post_id},
        function (data){
          data = $.parseJSON(data);
          if (data.code == 0){
              window.location.reload();
          }
        }
    );
}


//置顶
function setTop(){
    $.post(
      CONTEXT_PATH + "/discuss/top",
        {"id":$("#postId").val()},
        function (data){
          data = $.parseJSON(data);
          if (data.code == 0){
              $("#topBtn").attr("disabled","disabled");
          }else {
              alert(data.msg);
          }
        }
    );
}

//加精
function setWonderful(){
    $.post(
        CONTEXT_PATH + "/discuss/wonderful",
        {"id":$("#postId").val()},
        function (data){
            data = $.parseJSON(data);
            if (data.code == 0){
                $("#wonderfulBtn").attr("disabled","disabled");
            }else {
                alert(data.msg);
            }
        }
    );
}

//删除
function setDelete(){
    $.post(
        CONTEXT_PATH + "/discuss/delete",
        {"id":$("#postId").val()},
        function (data){
            data = $.parseJSON(data);
            if (data.code == 0){
                location.href = CONTEXT_PATH + "/index";
            }else {
                alert(data.msg);
            }
        }
    );
}

