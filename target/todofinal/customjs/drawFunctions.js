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