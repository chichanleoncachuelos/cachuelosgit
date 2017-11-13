/**
 * 
 */
var firstTime = true;
var scrollPos = 0;
function myTimeoutFunction()
{
	saveScrollPos();
	document.getElementById('usersender:load').click();
	document.getElementById('create:messageBeanMessageMessage').focus();
	setTimeout(myTimeoutFunction, 3000);
}
myTimeoutFunction();

function scrollToBottom() {
	if (firstTime){
		$('.ui-datatable-scrollable-body').scrollTop(100000);
		firstTime = false;
	}else{
		$('.ui-datatable-scrollable-body').scrollTop(100000);
	}			
}
function saveScrollPos() {
	scrollPos = $('.ui-datatable-scrollable-body').scrollTop();				
}