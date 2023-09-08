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
            makeCall("GET", "/VerbalizationRich_war_exploded/GoToTeacherHome", null,
                (x) => {
                    switch(x.status) {
                        case 200:
                            const responseAsJson = JSON.parse(x.responseText);
                            const courses = responseAsJson["courses"];

                            if (courses.length === 0) {
                                this.alert.show("Sembra che tu non abbia tenuto nessun corso");
                            } else {
                                // building the table containing all the useful data
                                this.updateCourses(courses);
                                this.pageOrchestrator.sortOrder = {
                                    subs_id: true,
                                    subs_name: true,
                                    subs_surname: true,
                                    subs_email: true,
                                    subs_degree: true,
                                    subs_grade: true,
                                    subs_state: true
                                }
                            }
                            break;
                        case 400:
                        case 500:
                        case 502:
                            this.alert.show(x.responseText);
                            break;
                        case 403:
                            window.location.href = x.getResponseHeader("Location");
                            window.sessionStorage.removeItem('userName');
                            window.sessionStorage.removeItem('userSurname');
                            window.sessionStorage.removeItem('userId');
                            window.sessionStorage.removeItem('userEmail');
                            window.sessionStorage.removeItem('userDegree');

                    }
                });
        };

        this.updateCourses = function(listOfCourses) {

            this.outSideContainer.style.visibility = "visible";
            this.greetingContainer.textContent = "Bentornato, " + sessionStorage.getItem("userName") + " " + sessionStorage.getItem("userSurname");
            this.courseTable.innerHTML = "";

            listOfCourses.forEach(
                (course) => {
                    let courseRow = this.courseTable.insertRow();
                    let courseIdTd = document.createElement("td");
                    let courseNameTd = document.createElement("td");
                    let courseNameAnchor = document.createElement("a");

                    let courseIdText = document.createTextNode(course["id"]);
                    let courseNameText = document.createTextNode(course["name"]);

                    courseIdTd.append(courseIdText);

                    courseNameAnchor.href = "#";
                    courseNameAnchor.addEventListener("click",
                        (e)=>{
                            this.pageOrchestrator.examSubscribersView.hide();
                            this.fetchExams(course["id"]);
                        });

                    courseNameTd.append(courseNameAnchor);
                    courseNameAnchor.appendChild(courseNameText);

                    courseRow.appendChild(courseIdTd);
                    courseRow.appendChild(courseNameTd);
                }
            );
            this.outSideContainer.style.visibility = "visibile";
            this.courseTable.style.visibility = "visible";
        }

        this.fetchExams = (courseId) => {
            makeCall("GET", "/VerbalizationRich_war_exploded/PresentTaughtExams?chosenCourse="+courseId, null,
                (x)=>{
                    switch(x.status) {
                        case 200:
                            const responseAsJson = JSON.parse(x.responseText);
                            const exams = responseAsJson["exams"];

                            if (exams.length === 0) {
                                this.alert.show("Sembra che per il corso selezionato non siano disponibili appelli");
                            } else {
                                // building the table containing all the useful data
                                this.loadTaughtExams(responseAsJson);
                            }
                            break;
                        case 400:
                        case 500:
                        case 502:
                            this.alert.show(x.responseText);
                            break;
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

        this.loadTaughtExams = (responseJson) => {

            let exams = responseJson["exams"];
            let chosenCourse = responseJson["chosenCourse"];

            this.examTable.innerHTML = "";

            let tableHead = this.examTable.createTHead();
            let tableHeadTh = document.createElement("th");
            let tableHeadText = document.createTextNode("Appelli di " + chosenCourse["name"]);

            tableHeadTh.appendChild(tableHeadText);
            tableHead.appendChild(tableHeadTh);

            let tableBody = this.examTable.createTBody();

            exams.forEach(
                (exam) =>{

                    let examRow = tableBody.insertRow();
                    let examTd = document.createElement("td");
                    let examAnchor = document.createElement("a");
                    let examText = document.createTextNode(exam["date"]);

                    examAnchor.href = "#";
                    examAnchor.addEventListener("click", (e) =>{
                        this.pageOrchestrator.sortOrder = {
                            subs_id: true,
                            subs_name: true,
                            subs_surname: true,
                            subs_email: true,
                            subs_degree: true,
                            subs_grade: true,
                            subs_state: true
                        }
                        this.pageOrchestrator.transitionToExamSubscribers(chosenCourse["id"], exam["date"]);
                    });

                    examAnchor.appendChild(examText);
                    examTd.append(examAnchor);
                    examRow.appendChild(examTd);

                });

            this.examTable.style.visibility = "visible";

        }

        this.hide = () => {
            this.outSideContainer.style.visibility = "hidden";
            this.courseTable.style.visibility = "hidden";
            this.examTable.style.visibility = "hidden";
        }

    }

    function ExamSubscribersView(options) {
        this.pageOrchestrator = options._pageOrchestrator;
        this.container = options._container;
        this.subsGreeting = options._greeting;
        this.subsTableHead = options._subsTableHead;
        this.subsTableBody = options._subsTableBody;
        this.subsPublishForm = options._subsPublishForm;
        this.subsPublishButton = options._subsPublishButton;
        this.subsPublishFormData = options._subsPublishFormData;
        this.subsVerbalizeForm = options._subsVerbalizeForm;
        this.subsVerbalizeButton = options._subsVerbalizeButton;
        this.subsVerbalizeFormData = options._subsVerbalizeFormData;
        this.subsMultiInsert = options._subsMultiInsert;
        this.alert = options._alertContainer;
        this.modalSingleGrade = options._singleGradeModal;
        this.modalSingleGradeGoBack = options._singleGradeGoBack;
        this.modalSingleGradeSubmit = options._singleGradeSubmit;
        this.modalSingleGradeGreeting = options._singleGradeGreeting;
        this.modalSingleGradeHead = options._singleGradeTableHead;
        this.modalSingleGradeBody = options._singleGradeTableBody;
        this.singleGradeForm = options._singleGradeForm;
        this.singleGradeFormData = options._singleGradeFormData;
        this.modalMultiGrade = options._multiGradeModal;
        this.modalMultiGradeGoBack = options._multiGradeGoBack;
        this.modalMultiGradeSubmit = options._multiGradeSubmit;
        this.modalMultiGradeGreeting = options._multiGradeGreeting;
        this.modalMultiGradeHead = options._multiGradeTableHead;
        this.modalMultiGradeBody = options._multiGradeTableBody;
        this.multiGradeForm = options._multiGradeForm;
        this.multiGradeFormData = options._multiGradeFormData;
        this.verbalizationModal = options._verbalizationModal;
        this.verbalizationAlert = options._verbalizationAlert;
        this.verbalizationGreeting = options._verbalizationGreeting;
        this.verbalizationTable = options._verbalizationTable;
        this.verbalizationTableBody = options._verbalizationTableBody;
        this.verbalizationGoBack = options._verbalizationGoBack;
        this.verbalizationVerbalTable = options._verbalizationVerbalTable;
        this.verbalizationVerbalTableBody = options._verbalizationVerbalTableBody;

        // setting the event listeners for the publishing, verbalize and multiInsert buttons


        this.subsPublishButton.addEventListener("click", (e) => {
            makeCall("POST", "/VerbalizationRich_war_exploded/PublishResults", this.subsPublishForm,
                (x) => {
                    switch(x.status) {
                        case 200:
                            let responseAsJson = JSON.parse(x.responseText);
                            this.show(responseAsJson["course"], responseAsJson["examDate"]);
                            break;
                        case 400:
                        case 500:
                        case 502:
                            this.alert.show(x.responseText);
                            break;
                        case 403:
                            window.location.href = x.getResponseHeader("Location");
                            window.sessionStorage.removeItem('userName');
                            window.sessionStorage.removeItem('userSurname');
                            window.sessionStorage.removeItem('userId');
                            window.sessionStorage.removeItem('userEmail');
                            window.sessionStorage.removeItem('userDegree');

                    }
                });
        });

        this.subsVerbalizeButton.addEventListener("click", (e) => {
            makeCall("POST", "/VerbalizationRich_war_exploded/VerbalizeResults", this.subsVerbalizeForm,
                (x) => {
                    switch(x.status) {
                        case 200:
                            let responseAsJson = JSON.parse(x.responseText);
                            let course = responseAsJson["course"];
                            this.show(course["id"], responseAsJson["examDate"]);
                            this.launchVerbalizationModal(responseAsJson);
                            break;
                        case 400:
                        case 500:
                        case 502:
                            this.alert.show(x.responseText);
                            break;
                        case 403:
                            window.location.href = x.getResponseHeader("Location");
                            window.sessionStorage.removeItem('userName');
                            window.sessionStorage.removeItem('userSurname');
                            window.sessionStorage.removeItem('userId');
                            window.sessionStorage.removeItem('userEmail');
                            window.sessionStorage.removeItem('userDegree');
                    }
                });
        });

        this.launchModalMultiInsert = function (subs, course, examDate) {
            this.modalMultiGradeGoBack.style.visibility = "visible";
            this.modalMultiGrade.style.display = "block";
            this.modalMultiGradeBody.innerHTML="";
            this.multiGradeFormData.innerHTML = "";
            if (subs.length === 0) {
                this.modalMultiGradeGreeting.textContent = "Non sono presenti iscritti con voto NON INSERITO";
                this.modalMultiGradeGreeting.style.visibility = "visible";
                this.multiGradeForm.style.visibility = "hidden";
                this.modalMultiGradeHead.style.visibility = "hidden";
                this.modalMultiGradeBody.style.visibility = "hidden";
            } else {

                this.modalMultiGradeSubmit.style.visibility = "visible";
                this.multiGradeForm.style.visibility = "visible";


                this.modalMultiGradeGreeting.textContent = "Seleziona gli studenti ed il voto per eseguire un inserimento multiplo";
                this.modalMultiGradeGreeting.style.visibility = "visible";

                // populating the table

                subs.forEach((sub)=>{
                    let row = this.modalMultiGradeBody.insertRow();
                    let selectionTd = document.createElement("td");
                    let idTd = document.createElement("td");
                    let nameTd = document.createElement("td");
                    let surnameTd = document.createElement("td");

                    let selectionInput = document.createElement("input");
                    selectionInput.type = "checkbox";
                    selectionInput.value = sub["id"];
                    selectionInput.name = "studentId";
                    let idText = document.createTextNode(sub["id"]);
                    let nameText = document.createTextNode(sub["name"]);
                    let surnameText = document.createTextNode(sub["surname"]);

                    selectionTd.appendChild(selectionInput);
                    idTd.appendChild(idText);
                    nameTd.appendChild(nameText);
                    surnameTd.appendChild(surnameText);

                    row.appendChild(selectionTd);
                    row.appendChild(idTd);
                    row.appendChild(nameTd);
                    row.appendChild(surnameTd);
                })

                this.modalMultiGradeHead.style.visibility = "visible";
                this.modalMultiGradeBody.style.visibility = "visible";

                // setting the form element
                let formDataCourseId = document.createElement("input");
                formDataCourseId.type = "hidden";
                formDataCourseId.value = course["id"];
                formDataCourseId.name = "courseId";

                this.multiGradeFormData.appendChild(formDataCourseId);

                let formDataExamDate = document.createElement("input");
                formDataExamDate.type = "hidden";
                formDataExamDate.value = examDate;
                formDataExamDate.name = "examDate";

                this.multiGradeFormData.appendChild(formDataExamDate);
            }

        };

        this.launchModalSingleInsert = function (student, course, examDate) {

            this.modalSingleGradeSubmit.style.visibility = "visible";
            this.modalSingleGradeGoBack.style.visibility = "visible";
            this.modalSingleGrade.style.display = "block"
            this.singleGradeForm.style.visibility = "visible";
            this.singleGradeFormData.innerHTML = "";

            // the greeting is displayed
            this.modalSingleGradeGreeting.textContent = "Inserisci il voto di " + student["name"] + " " + student["surname"] + " [Matricola " + student["id"]+"]";
            this.modalSingleGradeGreeting.style.visibility = "visible";
            this.modalSingleGradeBody.innerHTML = "";

            // the table containing student data is displayed
            let bodyRow;
            let rowTh;
            let rowThText;
            let rowTd;
            let rowTdText;
            // id
            bodyRow = this.modalSingleGradeBody.insertRow();
            rowTh = document.createElement("th");
            rowThText = document.createTextNode("Matricola");
            rowTd = document.createElement("td");
            rowTdText = document.createTextNode(student["id"]);

            rowTh.append(rowThText);
            rowTd.append(rowTdText);
            bodyRow.appendChild(rowTh);
            bodyRow.appendChild(rowTd);
            // name
            bodyRow = this.modalSingleGradeBody.insertRow();
            rowTh = document.createElement("th");
            rowThText = document.createTextNode("Nome");
            rowTd = document.createElement("td");
            rowTdText = document.createTextNode(student["name"]);

            rowTh.append(rowThText);
            rowTd.append(rowTdText);
            bodyRow.appendChild(rowTh);
            bodyRow.appendChild(rowTd);

            // surname
            bodyRow = this.modalSingleGradeBody.insertRow();
            rowTh = document.createElement("th");
            rowThText = document.createTextNode("Cognome");
            rowTd = document.createElement("td");
            rowTdText = document.createTextNode(student["surname"]);

            rowTh.append(rowThText);
            rowTd.append(rowTdText);
            bodyRow.appendChild(rowTh);
            bodyRow.appendChild(rowTd);
            // email
            bodyRow = this.modalSingleGradeBody.insertRow();
            rowTh = document.createElement("th");
            rowThText = document.createTextNode("Email");
            rowTd = document.createElement("td");
            rowTdText = document.createTextNode(student["email"]);

            rowTh.append(rowThText);
            rowTd.append(rowTdText);
            bodyRow.appendChild(rowTh);
            bodyRow.appendChild(rowTd);
            // degree
            bodyRow = this.modalSingleGradeBody.insertRow();
            rowTh = document.createElement("th");
            rowThText = document.createTextNode("Corso di Laurea");
            rowTd = document.createElement("td");
            rowTdText = document.createTextNode(student["degree"]);

            rowTh.append(rowThText);
            rowTd.append(rowTdText);
            bodyRow.appendChild(rowTh);
            bodyRow.appendChild(rowTd);
            // grade
            if (student["gradeStatus"]==="INSERITO") {
                bodyRow = this.modalSingleGradeBody.insertRow();
                rowTh = document.createElement("th");
                rowThText = document.createTextNode("Voto");
                rowTd = document.createElement("td");
                rowTdText = document.createTextNode(student["grade"]);
            }

            rowTh.append(rowThText);
            rowTd.append(rowTdText);
            bodyRow.appendChild(rowTh);
            bodyRow.appendChild(rowTd);

            this.modalSingleGradeHead.style.visibility = "visible";
            this.modalSingleGradeBody.style.visibility = "visible";

            let formDataCourseId = document.createElement("input");
            formDataCourseId.type = "hidden";
            formDataCourseId.value = course["id"];
            formDataCourseId.name = "courseId";

            this.singleGradeFormData.appendChild(formDataCourseId);

            let formDataExamDate = document.createElement("input");
            formDataExamDate.type = "hidden";
            formDataExamDate.value = examDate;
            formDataExamDate.name = "examDate";

            this.singleGradeFormData.appendChild(formDataExamDate);

            let formDataStudentId = document.createElement("input");
            formDataStudentId.type = "hidden";
            formDataStudentId.value = student["id"];
            formDataStudentId.name = "studentId";

            this.singleGradeFormData.appendChild(formDataStudentId);
        };

        this.subsMultiInsert.addEventListener("click", (e) => {
            this.launchModalMultiInsert(studentsWithNotInsertedGrade, selectedCourse, selectedExamDate);
        });

        this.modalMultiGradeGoBack.addEventListener("click", (e)=>{
            this.modalMultiGrade.style.display = "none";
            this.modalMultiGradeGoBack.style.visibility = "hidden";
            this.modalMultiGradeSubmit.style.visibility = "hidden";
        });

        this.modalMultiGradeSubmit.addEventListener("click", (e)=>{
            this.modalMultiGrade.style.display = "none";
            this.modalMultiGradeGoBack.style.visibility = "hidden";
            this.modalMultiGradeSubmit.style.visibility = "hidden";

            makeCall("POST", "/VerbalizationRich_war_exploded/MultiGradeInsert", this.multiGradeForm,
                (x)=>{
                    switch(x.status) {
                        case 200:
                            let responseAsJson = JSON.parse(x.responseText);
                            this.modalMultiGradeBody.innerHTML = "";
                            this.show(responseAsJson["course"], responseAsJson["examDate"]);
                            break;
                        case 400:
                        case 500:
                        case 502:
                            this.alert.show(x.responseText);
                            break;
                        case 403:
                            window.location.href = x.getResponseHeader("Location");
                            window.sessionStorage.removeItem('userName');
                            window.sessionStorage.removeItem('userSurname');
                            window.sessionStorage.removeItem('userId');
                            window.sessionStorage.removeItem('userEmail');
                            window.sessionStorage.removeItem('userDegree');
                    }
                });

        });


        this.modalSingleGradeGoBack.addEventListener("click", (e)=>{
            this.modalSingleGrade.style.display = "none";
            this.modalSingleGradeGoBack.style.visibility = "hidden";
            this.modalSingleGradeSubmit.style.visibility = "hidden";
        })

        this.modalSingleGradeSubmit.addEventListener("click", (e)=>{
            this.modalSingleGrade.style.display = "none";
            this.modalSingleGradeGoBack.style.visibility = "hidden";
            this.modalSingleGradeSubmit.style.visibility = "hidden";

            makeCall("POST", "/VerbalizationRich_war_exploded/UpdateGrade", this.singleGradeForm,
                (x)=>{
                    switch(x.status) {
                        case 200:
                            let responseAsJson = JSON.parse(x.responseText);
                            this.show(responseAsJson["course"], responseAsJson["examDate"]);
                            break;
                        case 400:
                        case 500:
                        case 502:
                            this.alert.show(x.responseText);
                            break;
                        case 403:
                            window.location.href = x.getResponseHeader("Location");
                            window.sessionStorage.removeItem('userName');
                            window.sessionStorage.removeItem('userSurname');
                            window.sessionStorage.removeItem('userId');
                            window.sessionStorage.removeItem('userEmail');
                            window.sessionStorage.removeItem('userDegree');
                    }
                }
            );
        })

        this.verbalizationGoBack.addEventListener("click", (e) => {
            this.verbalizationModal.style.display = "none";
            this.verbalizationGreeting.style.hidden = "hidden";
            this.verbalizationTable.style.hidden = "hidden";
            this.verbalizationTableBody.style.hidden = "hidden";
            this.verbalizationAlert.style.visibility = "hidden";
            this.verbalizationGoBack.style.visibility = "hidden";
        });

        this.launchVerbalizationModal = (responseJson) => {
            this.verbalizationModal.style.display = "block";

            let course = responseJson["course"];
            let examDate = responseJson["examDate"] ;
            let verbal = responseJson["verbal"];
            let verbalId = responseJson["verbalId"] ;
            let subs = responseJson["subscribers"];


            this.verbalizationGreeting.textContent = "Verbale dell'esame di "+course["name"]+" del giorno "+examDate;
            this.verbalizationGreeting.style.visibility = "visible";
            this.verbalizationVerbalTableBody.innerHTML = "";
            this.verbalizationTableBody.innerHTML = "";

            if (verbalId === -1) {
                this.verbalizationAlert.textContent = "Nessun risultato è stato verbalizzato";
                this.verbalizationAlert.style.visibility = "visible";
                this.verbalizationVerbalTableBody.style.visibility = "hidden";
                this.verbalizationVerbalTable.style.visibility = "hidden";
                this.verbalizationTable.style.visibility = "hidden";
                this.verbalizationTableBody.style.visibility = "hidden";
            } else {



                let verbalRow = this.verbalizationVerbalTableBody.insertRow();

                let verbalIdTh = document.createElement("th");
                let dateTimeIdTh = document.createElement("th");

                let verbalIdText = document.createTextNode(verbalId);
                let dateTimeText = document.createTextNode(verbal["date"]);

                verbalIdTh.append(verbalIdText);
                dateTimeIdTh.append(dateTimeText);

                verbalRow.appendChild(verbalIdTh);
                verbalRow.appendChild(dateTimeIdTh);

                subs.forEach((sub) => {
                    let row = this.verbalizationTableBody.insertRow();

                    let idTd = document.createElement("td");
                    let nameTd = document.createElement("td");
                    let surnameTd = document.createElement("td");
                    let gradeTd = document.createElement("td");

                    let idText = document.createTextNode(sub["id"]);
                    let nameText = document.createTextNode(sub["name"]);
                    let surnameText = document.createTextNode(sub["surname"]);
                    let gradeText = document.createTextNode(sub["grade"]);

                    idTd.append(idText);
                    nameTd.append(nameText);
                    surnameTd.append(surnameText);
                    gradeTd.append(gradeText);

                    row.appendChild(idTd);
                    row.appendChild(nameTd);
                    row.appendChild(surnameTd);
                    row.appendChild(gradeTd);

                });

                this.verbalizationVerbalTableBody.style.visibility = "visible";
                this.verbalizationVerbalTable.style.visibility = "visible";
                this.verbalizationTable.style.visibility = "visible";
                this.verbalizationTableBody.style.visibility = "visible";
                this.verbalizationAlert.style.visibility = "hidden";
            }

            this.verbalizationGoBack.style.visibility = "visible";
        }

        this.show = (courseId, examDate) => {
            this.alert.hide();
            makeCall("GET", "/VerbalizationRich_war_exploded/GoToExamSubscribers?courseId="+courseId+"&examDate="+examDate, null,
                (x)=>{
                    switch(x.status) {
                        case 200:
                            const responseAsJson = JSON.parse(x.responseText);
                            // building the table containing all the useful data
                            let subs = responseAsJson["subscribers"];
                            let course = responseAsJson["course"];
                            let examDate = responseAsJson["date"];
                            this.manageSubscriberData(subs, course, examDate);
                            break;
                        case 400:
                        case 500:
                        case 502:
                            this.alert.show(x.responseText);
                            break;
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

        this.manageSubscriberData = (_subs, _course, _examDate) => {
            let subs = _subs;
            let course = _course;
            let examDate = _examDate;

            selectedCourse = _course;
            selectedExamDate = _examDate;

            // present the greeting
            this.subsGreeting.textContent = "Iscritti all'esame di "+course["name"]+" del giorno " + examDate;
            this.subsGreeting.style.visibility = "visible";

            this.subsTableBody.innerHTML = "";
            // building the publish button

            this.subsTableBody.style.visibility = "hidden";
            this.subsTableHead.style.visibility = "hidden";
            this.subsPublishForm.style.visibility = "hidden";
            this.subsVerbalizeForm.style.visibility = "hidden";
            this.subsPublishButton.style.visibility = "hidden";
            this.subsVerbalizeButton.style.visibility = "hidden";
            this.subsMultiInsert.style.visibility = "hidden";

            if (subs.length === 0) {
                this.alert.show("Sembra che per l'appello selezionato non siano presenti iscritti");
                this.multiGradeForm.style.visibility = "hidden";
                this.modalMultiGradeHead.style.visibility = "hidden";
            } else {

                this.subsPublishFormData.innerHTML = "";
                let pubCourseHiddenInput = document.createElement("input");
                pubCourseHiddenInput.name = "courseId";
                pubCourseHiddenInput.type = "hidden";
                pubCourseHiddenInput.value = course["id"];
                let pubDateHiddenInput = document.createElement("input");

                pubDateHiddenInput.name = "examDate";
                pubDateHiddenInput.type = "hidden";
                pubDateHiddenInput.value = examDate;
                this.subsPublishFormData.appendChild(pubCourseHiddenInput);

                this.subsPublishFormData.appendChild(pubDateHiddenInput);
                // building the verbalization button

                this.subsVerbalizeFormData.innerHTML = "";
                let verbCourseHiddenInput = document.createElement("input");
                verbCourseHiddenInput.name = "courseId";
                verbCourseHiddenInput.type = "hidden";
                verbCourseHiddenInput.value = course["id"];
                let verbDateHiddenInput = document.createElement("input");

                verbDateHiddenInput.name = "examDate";
                verbDateHiddenInput.type = "hidden";
                verbDateHiddenInput.value = examDate;
                this.subsVerbalizeFormData.appendChild(verbCourseHiddenInput);

                this.subsVerbalizeFormData.appendChild(verbDateHiddenInput);
                // showing the buttons


                this.subsPublishForm.style.visibility = "visible";
                this.subsPublishFormData.style.visibility = "visible";
                this.subsPublishButton.style.visibility = "visible";
                this.subsVerbalizeForm.style.visibility = "visible";


                this.subsVerbalizeFormData.style.visibility = "visible";
                this.subsVerbalizeButton.style.visibility = "visible";
                this.subsMultiInsert.style.visibility = "visible";
                this.subsTableBody.innerHTML = "";
                studentsWithNotInsertedGrade = [];

                subs.forEach( (sub) => {
                    let subRow = this.subsTableBody.insertRow();

                    let subIdTd = document.createElement("td");
                    let subNameTd = document.createElement("td");
                    let subSurnameTd = document.createElement("td");
                    let subEmailTd = document.createElement("td");
                    let subDegreeTd = document.createElement("td");
                    let subGradeTd = document.createElement("td");
                    let subStatusTd = document.createElement("td");
                    let subActionTd = document.createElement("td");

                    let subIdText = document.createTextNode(sub["id"]);
                    let subNameText = document.createTextNode(sub["name"]);
                    let subSurnameText = document.createTextNode(sub["surname"]);
                    let subEmailText = document.createTextNode(sub["email"]);
                    let subDegreeText = document.createTextNode(sub["degree"]);
                    let subGradeText;

                    if (sub["gradeStatus"] === "NON INSERITO") {
                        studentsWithNotInsertedGrade.push(sub);
                        subGradeText = document.createTextNode(" ");
                    } else {
                        subGradeText = document.createTextNode(sub["grade"]);
                    }

                    let subStatusText = document.createTextNode(sub["gradeStatus"]);

                    let subActionText ;
                    let isButtonRequired = false;
                    if (sub["gradeStatus"] === "NON INSERITO") {
                        subActionText = document.createTextNode("Inserisci il voto");
                        isButtonRequired = true;
                    } else if (sub["gradeStatus"] === "INSERITO") {
                        subActionText = document.createTextNode("Modifica il voto");
                        isButtonRequired = true;
                    } else {
                        isButtonRequired = false;
                        subActionText = document.createTextNode("Nessuna azione disponibile");
                        subActionTd.append(subActionText);
                    }

                    if(isButtonRequired) {
                        let subActionButton = document.createElement("button");
                        subActionButton.type = "button";
                        subActionButton.className = "btn btn-outline-dark";
                        subActionButton.addEventListener("click", (e)=>{
                            this.launchModalSingleInsert(sub, course, examDate);
                        });

                        subActionButton.appendChild(subActionText);
                        subActionTd.append(subActionButton);
                    }

                    // display the button if necessary
                    subIdTd.append(subIdText);
                    subNameTd.append(subNameText);
                    subSurnameTd.append(subSurnameText);
                    subEmailTd.append(subEmailText);
                    subDegreeTd.append(subDegreeText);
                    subGradeTd.append(subGradeText);
                    subStatusTd.append(subStatusText);

                    subRow.appendChild(subIdTd);
                    subRow.appendChild(subNameTd);
                    subRow.appendChild(subSurnameTd);
                    subRow.appendChild(subEmailTd);
                    subRow.appendChild(subDegreeTd);
                    subRow.appendChild(subGradeTd);
                    subRow.appendChild(subStatusTd);
                    subRow.appendChild(subActionTd);

                });

                this.subsTableBody.style.visibility = "visible";
                this.subsTableHead.style.visibility = "visible";
            }
        }

        this.hide = () => {
            this.container.style.visibility = "hidden";
            this.subsGreeting.style.visibility = "hidden";
            this.subsTableHead.style.visibility = "hidden";
            this.subsTableBody.style.visibility = "hidden";
            // publishing button
            this.subsPublishForm.style.visibility = "hidden";
            this.subsPublishButton.style.visibility = "hidden";
            this.subsPublishFormData.style.visibility = "hidden";
            // verbalization button
            this.subsVerbalizeForm.style.visibility = "hidden";
            this.subsVerbalizeButton.style.visibility = "hidden";
            this.subsVerbalizeFormData.style.visibility = "hidden";
            // multi insert button
            this.subsMultiInsert.style.visibility = "hidden";
            this.alert.hide();
            // single grade insertion
            this.modalSingleGrade.style.display = "none";
            this.modalSingleGradeGoBack.style.visibility = "hidden";
            this.modalSingleGradeSubmit.style.visibility = "hidden";
            this.modalSingleGradeGreeting.style.visibility = "hidden";
            this.modalSingleGradeHead.style.visibility = "hidden";
            this.modalSingleGradeBody.style.visibility = "hidden";
            this.singleGradeForm.style.visibility = "hidden";
            this.singleGradeFormData.style.visibility = "hidden";
            // multi insertion
            this.modalMultiGrade.style.display = "none";
            this.modalMultiGradeGoBack.style.visibility = "hidden";
            this.modalMultiGradeSubmit.style.visibility = "hidden";
            this.modalMultiGradeGreeting.style.visibility = "hidden";
            this.modalMultiGradeHead.style.visibility = "hidden";
            this.modalMultiGradeBody.style.visibility = "hidden";
            this.multiGradeForm.style.visibility = "hidden";
            this.multiGradeFormData.style.visibility = "hidden";
            // verbalization modal
            this.verbalizationModal.style.display = "none";
            this.verbalizationAlert.style.visibility = "hidden";
            this.verbalizationGreeting.style.visibility = "hidden";
            this.verbalizationTable.style.visibility = "hidden";
            this.verbalizationTableBody.style.visibility = "hidden";
            this.verbalizationGoBack.style.visibility = "hidden";
            this.verbalizationVerbalTable.style.visibility = "hidden"
            this.verbalizationVerbalTableBody.style.visibility = "hidden"
        }
    }

    // and finally the page orchestrator is built
    function PageOrchestrator() {

        const logoutButton = document.getElementById("logout_button");
        this.logout = new LogoutButton(logoutButton);

        const alertContainer = document.getElementById("alertLabel");
        this.alertBox = new AlertBox(alertContainer);

        // accessing elements related to the courses and exams list
        const coursesTable = document.getElementById("teacher_courseTable");
        const examTable = document.getElementById("teacher_examTable");
        const greeting = document.getElementById("teacher_courses_greeting");
        const outSideCourseContainer = document.getElementById("courses");

        this.coursesExamsList = new CoursesExamsList(outSideCourseContainer, this.alertBox, coursesTable, examTable, greeting, this);

        // accessing elements related to the exam subscribers
        this.examSubscribersView = new ExamSubscribersView(options = {
            _pageOrchestrator: this,
            _container : document.getElementById("examSubscribers"),
            _greeting : document.getElementById("subs_greeting"),
            _subsTableHead : document.getElementById("subs_tableHead"),
            _subsTableBody : document.getElementById("subs_tableBody"),
            _subsPublishForm : document.getElementById("subs_publishForm"),
            _subsPublishButton : document.getElementById("subs_publishButton"),
            _subsPublishFormData : document.getElementById("subs_publishForm_hiddenData"),
            _subsVerbalizeForm : document.getElementById("subs_verbalizeForm"),
            _subsVerbalizeButton : document.getElementById("subs_verbalizeButton"),
            _subsVerbalizeFormData : document.getElementById("subs_verbalizeForm_hiddenData"),
            _subsMultiInsert : document.getElementById("subs_multiInsertGrades"),
            _singleGradeModal : document.getElementById("modal_SingleGrade"),
            _singleGradeGoBack : document.getElementById("modal_SingleGrade_goBack"),
            _singleGradeSubmit : document.getElementById("modal_SingleGrade_submit"),
            _singleGradeTableHead : document.getElementById("singleGrade_head"),
            _singleGradeTableBody : document.getElementById("singleGrade_body"),
            _singleGradeGreeting : document.getElementById("modal_singleGrade_greeting"),
            _singleGradeForm : document.getElementById("singleGrade_form"),
            _singleGradeFormData : document.getElementById("singleGrade_formData"),
            _multiGradeModal : document.getElementById("modal_multiGrade"),
            _multiGradeGoBack : document.getElementById("modal_multiGrade_goBack"),
            _multiGradeSubmit : document.getElementById("modal_multiGrade_submit"),
            _multiGradeTableHead : document.getElementById("multiGrade_head"),
            _multiGradeTableBody : document.getElementById("multiGrade_body"),
            _multiGradeGreeting : document.getElementById("modal_multiGrade_greeting"),
            _multiGradeForm : document.getElementById("multiGrade_form"),
            _multiGradeFormData : document.getElementById("multiGrade_formData"),
            _verbalizationModal : document.getElementById("modal_verbalization"),
            _verbalizationAlert : document.getElementById("verbalization_alert"),
            _verbalizationGreeting : document.getElementById("verbalization_greeting"),
            _verbalizationVerbalTable : document.getElementById("verbalization_verbalTable"),
            _verbalizationVerbalTableBody : document.getElementById("verbalization_VerbalTablebody"),
            _verbalizationTable : document.getElementById("verbalization_table"),
            _verbalizationTableBody : document.getElementById("verbalization_body"),
            _verbalizationGoBack : document.getElementById("verbalization_goBack"),
            _alertContainer : this.alertBox
        })

        // true when ascending, false when descending
        this.sortOrder = {
            subs_id: true,
            subs_name: true,
            subs_surname: true,
            subs_email: true,
            subs_degree: true,
            subs_grade: true,
            subs_state: true
        }

        let studentsWithNotInsertedGrade = [];
        let selectedCourse;
        let selectedExamDate;

        let tableElements = document.getElementsByClassName("sortable");

        console.log("Adding events");
        for (let i = tableElements.length - 1; i >= 0; i--) {
            console.log("Added event for "+ tableElements[i].id);
            tableElements[i].addEventListener("click",
                () => {
                    sortTable(tableElements[i].id, this.sortOrder);
                });
        }

        // starting the page orchestrator
        this.start = ()=>{
            // instantiate and hide everything
            this.alertBox.hide();
            this.coursesExamsList.hide();
            this.examSubscribersView.hide();

            this.coursesExamsList.show();
        };

        this.transitionToExamSubscribers = (courseId, examDate)=>{
            this.examSubscribersView.show(courseId, examDate);
        }
    }
}