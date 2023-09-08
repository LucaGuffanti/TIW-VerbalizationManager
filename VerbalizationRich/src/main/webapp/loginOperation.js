/**
 * Javascript file handling the login operation that occurs by pressing
 * the "login" button in "index.html"
 */

(function() {
	document.getElementById("errorMessage").style.visibility="hidden";
	document.getElementById("loginButton").addEventListener("click",
	(e) => {
		var relatedForm = e.target.closest("form");
		if(relatedForm.checkValidity() ) {
			console.log("making call");
			makeCall("POST", "/VerbalizationRich_war_exploded/CheckLogin", e.target.closest("form"),
			(x) => {
				if (x.readyState === XMLHttpRequest.DONE) {
					var response = x.responseText;
					switch(x.status) {
						case 200:
							let responseJson = JSON.parse(response);
								sessionStorage.setItem("userName", responseJson["name"]);
								sessionStorage.setItem("userSurname", responseJson["surname"]);
								sessionStorage.setItem("userId", responseJson["id"]);
								sessionStorage.setItem("userEmail", responseJson["email"]);
								sessionStorage.setItem("userDegree", responseJson["degree"]);
							console.log(responseJson);
							if (responseJson["isTeacher"]) {
								window.location.href = "teacher.html"
							} else {
								window.location.href = "student.html"
							}
							break;
						case 400:
						case 401:
						case 502:
							document.getElementById("errorMessage").style.visibility="visible";
							console.log(response);
							document.getElementById("errorMessage").textContent = response;
							break;
						default:
							console.log("got " + x.status);
					}
				}
			});
		} else {
			relatedForm.reportValidity();
			relatedForm.reset();
		}
	});
})();