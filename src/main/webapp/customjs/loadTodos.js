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

            if(jsonObject["redirect"]){
                console.log("Redirecting!");
                var redirectUrl = jsonObject["redirect"];
                window.location = redirectUrl;
                return;
            }

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

function populateText(){
    loadDoc();
}