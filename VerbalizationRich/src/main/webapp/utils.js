/**
 * Call management (AJAX)
 */

function makeCall(method, url, formElement, cback, reset = true) {
	let req = new XMLHttpRequest();
	req.onreadystatechange = function() {
		if (req.readyState === 4) {
			cback(req);
		}
	}; // closure
	req.open(method, url);
	if (formElement == null) {
		req.send();
	} else {
		req.send(new FormData(formElement));
	}
	if (formElement !== null && reset === true) {
		formElement.reset();
	}
}
function sortTable(columnId, colOrder) {
	let th = document.getElementById(columnId);
	let table = th.closest('table');
	let rowHeaders = table.querySelectorAll('th');
	let columnIdx =  Array.from(rowHeaders).indexOf(th);

	let rowsArray = Array.from(table.querySelectorAll('tbody > tr'));

	rowsArray.sort(createComparer(columnIdx, colOrder[columnId], columnId));

	colOrder[columnId] = !colOrder[columnId];

	for (let i = 0; i < rowsArray.length; i++) {
		table.querySelector('tbody').appendChild(rowsArray[i]);
	}
}

function getCellValue(tr, idx) {
	return tr.children[idx].textContent; // idx indexes the columns of the tr row
}

function createComparer(idx, asc, colId) {

	let gradeOrder =  {
		" ": 0,
		"ASSENTE": 1,
		"RIMANDATO": 2,
		"RIPROVATO": 3,
		"18": 4,
		"19": 5,
		"20": 6,
		"21": 7,
		"22": 8,
		"23": 9,
		"24": 10,
		"25": 11,
		"26": 12,
		"27": 13,
		"28": 14,
		"29": 15,
		"30": 16,
		"30 E LODE": 17
	}

	let statusOrder = {
		"NON INSERITO" : 0,
		"INSERITO" : 1,
		"PUBBLICATO" : 2,
		"RIFIUTATO" : 3,
		"VERBALIZZATO" : 4
	}

	return (rowa, rowb) => {

		let v1 = getCellValue(asc ? rowa : rowb, idx);
		let	v2 = getCellValue(asc ? rowb : rowa, idx);

		// If name, surname, degree, email -> check lexicographical ordering
		if(colId === "subs_name" || colId === "subs_surname" || colId === "subs_email" || colId === "subs_degree") {
			return v1.toString().localeCompare(v2);// lexical comparison

		} else if (colId === "subs_grade"){ // If grade -> check the enumeration ordering
			if (rowa[colId] === "NON INSERITO") {
				return 1;
			} else if (rowb[colId] === "NON INSERITO") {
				return -1;
			} else {
				return gradeOrder[v1] - gradeOrder[v2];
			}
		} else if (colId === "subs_state"){ // If gradeStatus -> check the enumeration ordering
			return statusOrder[v1] - statusOrder[v2];
		}

		// If id -> check the number ordering
		return v1 - v2;
	};
}
