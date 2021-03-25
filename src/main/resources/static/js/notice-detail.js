function back(){
    location.href = CONTEXT_PATH + "/notice/list";
}

function dle(btn,fromId,toId){
    var topic = $("#dletopic1").val();
    $.post(
        CONTEXT_PATH + "/notice/delete/" + topic,
        {"fromId":fromId,"toId":toId},
        function (data){

        }
    );
    $(btn).parents(".media").remove();
}