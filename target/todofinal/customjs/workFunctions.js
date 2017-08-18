function doWork(){
    doProgress(this,"/progress");
}

function doDelete(){
    doProgress(this,"/delete");
}

function doProgress(caller,work){
    caller.disabled = true;
    caller.style.display = "none";

    if(clearId != -1) clearTimeout(clearId);

    var http = new XMLHttpRequest();
    var url = work;
    var params = "todoid=" + caller.id;

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