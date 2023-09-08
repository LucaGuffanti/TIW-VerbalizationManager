/**
 * 
 */
{
	window.addEventListener("load", () => {
		let pageOrchestrator = new PageOrchestrator();

		if (sessionStorage.getItem("userId") == null) {
	      window.location.href = "index.html";
	    } else {
	      pageOrchestrator.start(); // initialize the components
	    } // display initial content
	  }, false);
	
	// here every single view component is built
	
	function LogoutButton(_logoutButton) {
		this.button = _logoutButton;
		this.button.addEventListener("click", (e) => 
		{
			makeCall("GET", "/VerbalizationRich_war_exploded/Logout", null,
			() => {
				window.location.href = "index.html";
			});
		});
	}
	
	function AlertBox(_container) {
		this.container = _container;
		this.show = function(text) {
			this.container.style.visibility = "visible";
			this.container.textContent = text;
		}
		this.hide = function() {
			this.container.style.visibility = "hidden";
		};
	}
	
	
	function CoursesExamsList(_container ,_alert, _courseTable, _examTable, _greetingContainer, _pageOrchestrator) {
		this.outSideContainer = _container;
		this.alert = _alert;
		this.courseTable = _courseTable;
		this.examTable = _examTable;
		this.greetingContainer = _greetingContainer;
		this.pageOrchestrator = _pageOrchestrator;

		const self = this;
		this.show = () => {
			makeCall("GET", "/VerbalizationRich_war_exploded/GoToStudentHome", null,
			(x) => {
				switch(x.status) {
					case 200:
						const responseAsJson = JSON.parse(x.responseText);
						const courses = responseAsJson["courses"];

						if (courses.length === 0) {
							this.alert.show("Sembra che non ti sia iscritto ad alcun corso");
						} else {
							// building the table containing all the useful data
							this.updateCourses(courses);
						}
						break;
					case 400:
					case 500:
					case 502:
						this.alert.show(x.responseText);
						break;
					case 401:
						this.alert.show("Non sei autorizzato a vedere questa pagina. Premi sul bottone di logout");
					case 403:
						window.location.href = x.getResponseHeader("Location");
                  		window.sessionStorage.removeItem('userName');
                  		window.sessionStorage.removeItem('userSurname');
                  		window.sessionStorage.removeItem('userId');
                  		window.sessionStorage.removeItem('userEmail');
                  		window.sessionStorage.removeItem('userDegree');
						break;

				}
			});
		};

		this.updateCourses = function(listOfCourses) {

			let row, idCell, nameCell, nameAnchor, nameLink, teacherCell;
			const self = this;

			this.outSideContainer.style.visibility = "visible";
			this.greetingContainer.textContent = "Bentornato, " + sessionStorage.getItem("userName") + " " + sessionStorage.getItem("userSurname");
			this.courseTable.innerHTML = "";

			listOfCourses.forEach(
				(course) => {
					row = document.createElement("tr");

					idCell = document.createElement("td");
					idCell.textContent = course["id"];
					row.appendChild(idCell);

					nameCell = document.createElement("td");
					nameAnchor = document.createElement("a");
					nameLink = document.createTextNode(course["name"]);

					nameCell.append(nameAnchor);
					nameAnchor.appendChild(nameLink);
					nameAnchor.setAttribute("chosenCourse", course["id"]);
					nameAnchor.href = "#";
					nameAnchor.addEventListener("click", (e) => {
						this.pageOrchestrator.examResult.hide();
						this.updateExams(course["id"]);
					});
					row.appendChild(nameCell);

					teacherCell = document.createElement("td");
					teacherCell.textContent = (course["teacherName"] + " " + course["teacherSurname"]);
					row.appendChild(teacherCell);

					self.courseTable.appendChild(row);
				}
			);
			this.courseTable.style.visibility = "visible";
		}
		this.hide = function() {
			this.outSideContainer.style.visibility = "hidden";
			this.courseTable.style.visibility = "hidden";
			this.examTable.style.visibility = "hidden";
		}

		this.updateExams = (course) => {
			makeCall("GET", "/VerbalizationRich_war_exploded/PresentSubscribedExams?chosenCourse="+course, null,
				(x) => {
					switch(x.status) {
						case 200:
							const responseAsJson = JSON.parse(x.responseText);
							const exams = responseAsJson["exams"];
							const chosen = responseAsJson["chosenCourse"];

							if (exams.length === 0) {
								this.alert.show("Sembra non ti sia iscritto ad alcun corso per l'esame selezionato");
							} else {
								// building the table containing all the useful data
								this.showExams(exams, chosen);
							}
							break;
						case 400:
						case 500:
						case 502:
							this.alert.show(x.responseText);
							break;
						case 401:
							this.errorMessageContainer.show("Non sei autorizzato a vedere questa pagina. Premi sul bottone di logout");
						case 403:
							window.location.href = x.getResponseHeader("Location");
							window.sessionStorage.removeItem('userName');
							window.sessionStorage.removeItem('userSurname');
							window.sessionStorage.removeItem('userId');
							window.sessionStorage.removeItem('userEmail');
							window.sessionStorage.removeItem('userDegree');
					}
				});
		}

		this.showExams = (_exams, chosen) => {
			this.examTable.innerHTML = "";

			let tableHead = this.examTable.createTHead();
			let tableHeadRow = tableHead.insertRow();

			let tableHeadTh = document.createElement("th");
			let tableHeadText = document.createTextNode("Appelli di " + chosen["name"]);

			tableHeadTh.appendChild(tableHeadText);
			tableHeadRow.appendChild(tableHeadTh);

			let tableBody = this.examTable.createTBody();

			_exams.forEach(
				(exam) => {
					let tableBodyRow = tableBody.insertRow();
					let tableBodyTd = document.createElement("td");
					let examAnchor = document.createElement("a");


					let examDate = document.createTextNode(exam["date"]);

					examAnchor.appendChild(examDate);
					tableBodyTd.appendChild(examAnchor);
					tableBodyRow.appendChild(tableBodyTd);

					examAnchor.href = "#"
					examAnchor.addEventListener("click", ()=>{
						this.pageOrchestrator.transitionToResultView(chosen["id"] ,exam["date"]);
					});
				}
			);

			this.examTable.style.visibility = "visible";
		}
	}

	function ExamResult(options){
		this.resultGreetingContainer = options._resultGreetingContainer;
		this.resultCourseDataTable = options._resultCourseDataTable;
		this.resultCourseDataBody = options._resultCourseDataBody;
		this.resultStudentDataTable = options._resultStudentDataTable;
		this.resultStudentDataBody = options._resultStudentDataBody;
		this.resultGradeTable = options._resultGradeTable;
		this.resultGradeTableBody = options._resultGradeTableBody;
		this.resultRejectionForm = options._resultRejectionForm;
		this.resultRejectionInnerForm = options._resultRejectionInnerForm;
		this.resultRejectionButton = options._resultRejectionButton;
		this.resultAlert = options._resultAlert;
		this.errorMessageContainer = options._errorMessageContainer;

		this.resultRejectionForm.addEventListener("submit", (e) =>{
			e.preventDefault();
		})

		this.resultRejectionButton.addEventListener("click",
			(e)=> {
				e.preventDefault();
				makeCall("POST", "/VerbalizationRich_war_exploded/RejectResult", this.resultRejectionForm,
					(x)=>{
						switch(x.status) {
							case 200:
								let responseAsJson = JSON.parse(x.responseText);
								this.manageRejectionResult(responseAsJson);
								break;
							case 400:
							case 500:
							case 502:
								this.errorMessageContainer.show(x.responseText);
								break;
							case 401:
								this.errorMessageContainer.show("Non sei autorizzato a vedere questa pagina. Premi sul bottone di logout");
							case 403:
								window.location.href = x.getResponseHeader("Location");
								window.sessionStorage.removeItem('userName');
								window.sessionStorage.removeItem('userSurname');
								window.sessionStorage.removeItem('userId');
								window.sessionStorage.removeItem('userEmail');
								window.sessionStorage.removeItem('userDegree');
								break;
						}
					});
			});
		this.show = (courseId, date) => {
			makeCall("GET", "/VerbalizationRich_war_exploded/GoToExamResults?courseId="+courseId+"&examDate="+date, null,
				(x) =>{
					switch(x.status) {
						case 200:
							let responseAsJson = JSON.parse(x.responseText);
							this.manageResult(responseAsJson);
							break;
						case 400:
						case 500:
						case 502:
							this.errorMessageContainer.show(x.responseText);
							break;
						case 401:
							this.errorMessageContainer.show("Non sei autorizzato a vedere questa pagina. Premi sul bottone di logout");
						case 403:
							window.location.href = x.getResponseHeader("Location");
							window.sessionStorage.removeItem('userName');
							window.sessionStorage.removeItem('userSurname');
							window.sessionStorage.removeItem('userId');
							window.sessionStorage.removeItem('userEmail');
							window.sessionStorage.removeItem('userDegree');
							break;

					}
				});
		}

		this.manageResult = (responseJson) => {

			// retrieving data from json
			let courseData = responseJson["course"];
			let examDate = responseJson["date"];
			let teacherData = responseJson["teacher"];
			let gradeData = responseJson["grade"];
			let studentData = responseJson["student"];

			this.resultCourseDataBody.innerHTML = "";
			this.resultGradeTableBody.innerHTML = "";
			this.resultStudentDataBody.innerHTML = "";

			// building the rejection form
			this.resultRejectionInnerForm.innerHTML = "";

			let courseHiddenInput = document.createElement("input");
			courseHiddenInput.name = "courseId";
			courseHiddenInput.type = "hidden";
			courseHiddenInput.value = courseData["id"];

			let dateHiddenInput = document.createElement("input");
			dateHiddenInput.name = "examDate";
			dateHiddenInput.type = "hidden";
			dateHiddenInput.value = examDate;

			this.resultRejectionInnerForm.appendChild(courseHiddenInput);
			this.resultRejectionInnerForm.appendChild(dateHiddenInput);

			// building the greeting text
			this.resultGreetingContainer.textContent = "Appello di "+courseData["name"]+" del giorno "+examDate+" - ESITO";
			this.resultGreetingContainer.style.visibility = "visible";

			if (!(gradeData["gradeStatus"] === "NON INSERITO" || gradeData["gradeStatus"] === "INSERITO")) {
				// building the student data table
				this.resultStudentDataBody.innerHTML = "";

				let studentDataBodyRow = this.resultStudentDataBody.insertRow();

				let studentIdTd = document.createElement("td");
				let studentNameTd = document.createElement("td");
				let studentSurnameTd = document.createElement("td");
				let studentEmailTd = document.createElement("td");
				let studentDegreeTd = document.createElement("td");

				let studentId = document.textContent = studentData["id"];
				let studentName = document.textContent = studentData["name"];
				let studentSurname = document.textContent = studentData["surname"];
				let studentEmail = document.textContent = studentData["email"];
				let studentDegree = document.textContent = studentData["degree"];

				studentIdTd.append(studentId);
				studentNameTd.append(studentName);
				studentSurnameTd.append(studentSurname);
				studentEmailTd.append(studentEmail);
				studentDegreeTd.append(studentDegree);

				// adding student data to the row

				studentDataBodyRow.appendChild(studentIdTd);
				studentDataBodyRow.appendChild(studentNameTd);
				studentDataBodyRow.appendChild(studentSurnameTd);
				studentDataBodyRow.appendChild(studentEmailTd);
				studentDataBodyRow.appendChild(studentDegreeTd);

				// displaying the table
				this.resultStudentDataTable.style.visibility = "visible";
				this.resultStudentDataBody.style.visibility = "visible";

				// building the course data table
				this.resultCourseDataBody.innerHTML = "";

				let courseDataBodyRow = this.resultCourseDataBody.insertRow();

				let courseIdTd = document.createElement("td");
				let courseNameTd = document.createElement("td");
				let courseTeacherTd = document.createElement("td");

				let courseId = document.createTextNode(courseData["id"]);
				let courseName = document.createTextNode(courseData["name"]);
				let courseTeacher = document.createTextNode(teacherData["name"] + " " + teacherData["surname"]);

				courseIdTd.append(courseId);
				courseNameTd.append(courseName);
				courseTeacherTd.append(courseTeacher);

				courseDataBodyRow.appendChild(courseIdTd);
				courseDataBodyRow.appendChild(courseNameTd);
				courseDataBodyRow.appendChild(courseTeacherTd);

				this.resultCourseDataTable.style.visibility = "visible";
				this.resultCourseDataBody.style.visibility = "visible";

				// building the grade table
				this.resultGradeTableBody.innerHTML = "";

				let gradeTableBodyRow = this.resultGradeTableBody.insertRow();

				let gradeTd = document.createElement("td");
				let grade;

				grade = document.createTextNode(gradeData["grade"]);
				gradeTd.append(grade);

				gradeTableBodyRow.appendChild(gradeTd);

				this.resultGradeTable.style.visibility = "visible";
				this.resultGradeTableBody.style.visibility = "visible";

				this.resultRejectionForm.style.visibility = "hidden";
				this.resultRejectionButton.style.visibility = "hidden";

				// remove every class from the style
			}
			this.resultAlert.className = "";
			switch (gradeData["gradeStatus"]) {
				case "NON INSERITO":
				case "INSERITO":
					console.log("voto non inserito");
					this.resultCourseDataTable.style.visibility = "hidden";
					this.resultCourseDataBody.style.visibility = "hidden";
					this.resultGradeTable.style.visibility = "hidden";
					this.resultGradeTableBody.style.visibility = "hidden";
					this.resultStudentDataTable.style.visibility = "hidden";
					this.resultStudentDataBody.style.visibility = "hidden";
					this.resultAlert.className = "alert alert-primary";
					this.resultAlert.textContent = "Il voto non è ancora inserito";
					break;
				case "PUBBLICATO":
					this.resultAlert.className = "alert alert-primary";
					this.resultAlert.textContent = "Il voto è stato pubblicato";
					// if the grade is published and the student has passed the exam
					// the form regarding the rejection of the grade
					if (!(gradeData["grade"].toString() === "ASSENTE"
						|| gradeData["grade"].toString() === "RIMANDATO"
						|| gradeData["grade"].toString() === "RIPROVATO")) {
						this.resultRejectionForm.style.visibility = "visible";
						this.resultRejectionButton.style.visibility = "visible";
					}
					break;
				case "RIFIUTATO":
					this.resultAlert.className = "alert alert-danger";
					this.resultAlert.textContent = "Il voto è stato rifiutato";
					break;
				case "VERBALIZZATO":
					this.resultAlert.className = "alert alert-success";
					this.resultAlert.textContent = "Il voto è stato verbalizzato";
					break;
			}
			this.resultAlert.style.visibility = "visible";
		}

		this.manageRejectionResult = (responseAsJson) => {
			let alreadyVerbalized = responseAsJson["alreadyVerbalized"];

			// setting the alert container based on the result of the rejection

			this.resultRejectionInnerForm.style.visibility = "hidden";
			this.resultRejectionForm.style.visibility = "hidden";
			this.resultRejectionButton.style.visibility = "hidden";
			this.resultAlert.style.visibility = "hidden";

			this.resultAlert.className = "";
			if (alreadyVerbalized) {
				this.resultAlert.className = "alert alert-warning";
				this.resultAlert.textContent = "Non è possibile rifiutare il voto perché è già stato verbalizzato";
			} else {
				this.show(responseAsJson["courseId"], responseAsJson["examDate"]);
			}
			this.resultAlert.style.visibility = "visible";
		}

		this.hide = () => {
			this.resultGreetingContainer.style.visibility = "hidden";
			this.resultCourseDataTable.style.visibility = "hidden";
			this.resultCourseDataBody.style.visibility = "hidden";
			this.resultGradeTable.style.visibility = "hidden";
			this.resultGradeTableBody.style.visibility = "hidden";
			this.resultStudentDataTable.style.visibility = "hidden";
			this.resultStudentDataBody.style.visibility = "hidden";
			this.resultRejectionForm.style.visibility = "hidden";
			this.resultRejectionButton.style.visibility = "hidden";
			this.resultAlert.style.visibility = "hidden";
		}
	}

	// and finally the page orchestrator is built
	function PageOrchestrator() {

		const logoutButton = document.getElementById("logout_button");
		this.logout = new LogoutButton(logoutButton);

		const alertContainer = document.getElementById("alertLabel");
		this.alertBox = new AlertBox(alertContainer);

		// accessing elements related to the courses and exams list
		const coursesTable = document.getElementById("courseTable");
		const examTable = document.getElementById("examTable");
		const greeting = document.getElementById("courses_greeting");
		const outSideCourseContainer = document.getElementById("courses");

		this.coursesExamsList = new CoursesExamsList(outSideCourseContainer, this.alertBox, coursesTable, examTable, greeting, this);

		// accessing elements related to the exam result

		this.examResult = new ExamResult(options = {
			_resultGreetingContainer : document.getElementById("result_greeting"),
			_resultCourseDataTable : document.getElementById("result_courseDataTable"),
			_resultCourseDataBody : document.getElementById("result_courseDataBody"),
			_resultStudentDataTable : document.getElementById("result_studentDataTable"),
			_resultStudentDataBody : document.getElementById("result_studentDataBody"),
			_resultGradeTable : document.getElementById("result_gradeTable"),
			_resultGradeTableBody : document.getElementById("result_gradeTableBody"),
			_resultRejectionForm : document.getElementById("result_rejectionForm"),
			_resultRejectionInnerForm : document.getElementById("result_rejectionInnerForm"),
			_resultRejectionButton : document.getElementById("result_rejectionButton"),
			_resultAlert : document.getElementById("result_alert"),
			_errorMessageContainer : this.alertBox
		} );

		// starting the page orchestrator
		this.start = ()=>{
			// instantiate and hide everything
			this.alertBox.hide();
			this.coursesExamsList.hide();
			this.examResult.hide();

			this.coursesExamsList.show();
		};

		this.transitionToResultView = (course, date)=> {
			//this.coursesExamsList.hide();

			this.examResult.show(course, date);
		}
	}
}