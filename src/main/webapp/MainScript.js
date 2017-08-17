var allTodos = {};
allTodos["new"] = {};
allTodos["progress"] = {};
allTodos["complete"] = {};

var currentList = 0;
var currentChange = 0;
var clearId = -1;

function displayUsername(){
    var xhttp = new XMLHttpRequest();
    xhttp.onreadystatechange = function() {
        if(this.readyState == 4 && this.status == 200) {
            var jsonObject = JSON.parse(this.responseText);
            var username = jsonObject["result"];
            var elem = document.getElementById("usernameContain");
            elem.innerHTML = username;
        }
    }
    var url = "/getuser";
    xhttp.open("GET", url , true);
    xhttp.send();
}

function loadDoc(){
    //console.log("Making a new request");
    var xhttp = new XMLHttpRequest();
    var response = "";
    xhttp.onreadystatechange = function() {
        if (this.readyState == 4 && this.status == 200) {
            response = this.responseText;
            //console.log("Following is the response");
            //console.log(response);
            var jsonObject = JSON.parse(response);
            var todos = jsonObject["todos"];
            var changes = jsonObject["changes"];
            var finalPositions = jsonObject["changeposition"];
            var changeUsers = jsonObject["changeusername"];

            var numList = jsonObject["numlist"];
            numList = parseInt(numList[0]);
            var numChanges = jsonObject["changelist"];
            numChanges = parseInt(numChanges[0]);

            handleTodos(todos,numList);
            handleChanges(changes,finalPositions,changeUsers,numChanges);

            drawPage();
            clearId = setTimeout(loadDoc,5000);
        }else if(this.status >= 400 && this.status < 500){
            document.body.innerHTML = "You do not have access to the page. You should login first.";
        }
    };
    var url = "/main?";
    url += ("numlist=" + currentList);
    url += "&"
    url += ("changeslist=" + currentChange);
    xhttp.open("GET", url , true);
    xhttp.send();
}

function handleTodos(todos,numList){
    // Handles newly created todos
    currentList = numList;
    var l = todos.length;
    for(var i=0;i<l;i++){
        var todo = todos[i];
        allTodos[todo["category"]][todo["id"]] = todo ;
    }
}

function handleChanges(changes,finalPositions,changeUsers,numChanges){
    currentChange = numChanges;
    var l = changes.length;
    for(var i=0;i<l;i++){
        var todoId = parseInt(changes[i]);
        var finalPosition = (finalPositions[i]);
        var user = changeUsers[i];
        changeAllTodos(todoId,finalPosition,user);
    }
}

function changeAllTodos(todoId,finalPosition,changeUser){
    if(allTodos["new"][todoId] != null && allTodos["new"][todoId] != undefined){
        if(finalPosition == "delete"){
            allTodos["new"][todoId] = null;
            return;
        }
        var todo = allTodos["new"][todoId];
        todo["category"] = finalPosition;
        todo["userName"] = changeUser;
        allTodos["new"][todoId] = null;
        allTodos[finalPosition][todoId] = todo;
    }else if(allTodos["progress"][todoId] != null && allTodos["progress"][todoId] != undefined){
        if(finalPosition == "delete"){
            allTodos["progress"][todoId] = null;
            return;
        }
        var todo = allTodos["progress"][todoId];
        todo["category"] = finalPosition;
        todo["userName"] = changeUser;
        allTodos["progress"][todoId] = null;
        allTodos[finalPosition][todoId] = todo;
    }else{
        // Do nothing!
    }
}

function drawPage(){
    var new_tasks = document.getElementById("new_tasks_div");
    var progress_tasks = document.getElementById("progress_tasks_div");
    var complete_tasks = document.getElementById("complete_tasks_div");
    fillBox(new_tasks,"new"); fillBox(progress_tasks,"progress"); fillBox(complete_tasks,"complete");
}

function fillBox(taskCol,str) {
    while(taskCol.hasChildNodes()){
        taskCol.removeChild(taskCol.lastChild);
    }
    for(var key in allTodos[str]){
        if(key != undefined && key != null) {
            if (allTodos[str][key] != undefined && allTodos[str][key] != null) {
                if(allTodos[str][key]["category"] == str) taskCol.appendChild(getDiv(allTodos[str][key]));
            }
        }
    }
}

function getDiv(todoObj){
    var message = todoObj["message"];
    var username = todoObj["userName"];
    var id = todoObj["id"];
    var type = todoObj["category"];

    var elem = document.createElement("div");
    elem.className = "panel"; elem.id = id;
    elem.style.borderStyle = "solid";
    elem.style.borderRadius = "2px";
    elem.style.borderWidth = "thin";
    elem.style.margin = "4px";

    var subElem1 = document.createElement("div");
    subElem1.className = "panel-body"; subElem1.innerHTML = message;
    //subElem1.background = "#eae7e7";
    subElem1.minHeight = "20vh";

    var subElem2 = document.createElement("kbd");
    subElem2.innerHTML = ( username );
    subElem2.style.float = "right";

    elem.appendChild(subElem2); elem.appendChild(subElem1);

    if(!(type == "complete")){
        var btnDiv = document.createElement("div");

        var btnElem = document.createElement("button");
        btnElem.className = "btn btn-default";
        btnElem.name = type; btnElem.id = id;
        btnElem.onclick = doWork;

        var delElem = document.createElement("button");
        delElem.className = "btn btn-default pull-right";
        delElem.name = type; delElem.id = id;
        delElem.onclick = doDelete;
        delElem.innerHTML = "Delete";

        if(type == "new"){
            btnElem.innerHTML = "Work";
        }else{
            btnElem.innerHTML = "Complete";
        }
        btnDiv.appendChild(btnElem);
        btnDiv.appendChild(delElem);
        elem.appendChild(btnDiv);
    }
    return elem;
}

function doWork(){
    this.disabled = true;
    this.style.display = "none";

    if(clearId != -1) clearTimeout(clearId);

    var http = new XMLHttpRequest();
    var url = "/progress";
    var params = "todoid=" + this.id;

    http.onreadystatechange = function() {
        if(this.status == 200 && this.readyState == 4){
            var result = this.responseText;
            console.log(result);
            populateText();
            displayInfo(result);
        }
    }
    http.open("POST", url, true);
    //Send the proper header information along with the request
    http.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
    http.send(params);
}

function doDelete(){
    this.disabled = true;
    this.style.display = "none";

    if(clearId != -1) clearTimeout(clearId);

    var http = new XMLHttpRequest();
    var url = "/delete";
    var params = "todoid=" + this.id;

    http.onreadystatechange = function() {
        if(this.status == 200 && this.readyState == 4){
            var result = this.responseText;
            console.log(result);
            populateText();
            displayInfo(result);
        }
    }
    http.open("POST", url, true);
    //Send the proper header information along with the request
    http.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
    http.send(params);
}

function populateText(){
    loadDoc();
}

function displayInfo(result){
    var elem = document.getElementById("info");
    elem.innerHTML = result;
    console.log("Toggling the display element.");
    console.log(result);
    if(result != "") toggleDisplay("infoContain");
}

displayUsername();
populateText();

function toggleDisplay(id) {
    var e = document.getElementById(id);
    if(e.style.display == "block") {
        e.style.display = "none";
    } else {
        e.style.display = "block";
    }
}