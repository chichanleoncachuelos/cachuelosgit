/**
 * 
 */
function myTimeoutFunction()
{
	alert("Hello! I am an alert box!");
	document.getElementById('refreshall:load').click();
	setTimeout(myTimeoutFunction, 4000);
}
myTimeoutFunction();