/**
 * Created by glockett on 31/03/2016.
 */

$(document).ready(function(){

 //   $("#report tr:contains(pageclass)").addClass("odd");
    $("#report tr:not(.pageclass)").hide();
    $("#report tr:first-child").show();
    $("#report tr:last-child").show();

    $("#report tr.datarow").show();

    $("#report tr.alert").click(function(){
        $(this).next("tr").toggle();
        $(this).find(".arrow").toggleClass("up");
    });

//    $("#report").jExpand();
//    $(".data tr:odd").show();
//    $(".data tr:even").show();

});




