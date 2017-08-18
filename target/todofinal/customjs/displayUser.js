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