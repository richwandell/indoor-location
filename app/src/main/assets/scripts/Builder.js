;(function($){
    var canvas, overlay, database, debug = true, super_debug = false, image_name,
        selected_grid = [], full_grid = [], hover_grid = [], multi_selected_grid = [],
        image_width, image_height, image, vgrid_spaces, hgrid_spaces, floorplanname,
        selected_id, touch_cx, touch_cy, grid_lines_enabled = true, canvas_context,
        overlay_context, mouse_down = false, m_x_start = false, m_y_start = false;

    function init(){
        debug ? console.debug(arguments.callee.name) : '';

        //Grab and save our canvas
        canvas = $("#builder_canvas")[0];
        canvas_context = canvas.getContext('2d');
        overlay = $("#builder_canvas_overlay")[0];
        overlay_context = overlay.getContext('2d');

        $("#builder_canvas_container").css("maxWidth", $(window).width());

        if(builder_mode && builder_mode == 1) {
            //Request our db and set event handlers
            var dbrequest = window.indexedDB.open("BuilderDatabase", 6);
            dbrequest.onupgradeneeded = dbonupgradeneeded;
            dbrequest.onsuccess = dbonsuccess;
            dbrequest.onerror = dbonerror;
        }else if(builder_mode && builder_mode == 2){
            if(typeof Android != "undefined" && Android.getData) {
                // var s = Android.getData("fplan-11.json");
                // loadBuilderData(JSON.parse(s));
            }else{
                loadBuilderData();
            }
            bindTouchEvents();
            $("#builder_canvas_container").height($(window).height());
            return;
        }

        //Setup events for elements
        $("#builder_image_input").change(imageChanged);
        $("#builder_select_existing").change(selectChanged);
        $("#builder_zoom_in").click(zoomIn);
        $("#builder_zoom_out").click(zoomOut);
        $("#builder_delete_existing").click(deleteExisting);
        $("#builder_vgrid_spaces, #builder_hgrid_spaces").change(spacesChanged);
        $("#builder_add_spaces").click(addSpace);
        $("#builder_grid_color").change(redraw);
        var bngs = $("#builder_named_grid_spaces");
        bngs.on("click", "tr", selectGridFromList);
        bngs.on("mouseenter", "tr", hoverGridFromList);
        bngs.on("mouseleave", "tr", removeHoverGridFromList);
        bngs.on("click", "tr ul", toggleSpaceDisplay);
        $("#save_floorplan").click(saveFloorplan);
        $("#builder_download").click(downloadFloorplan);

        $(overlay).on({
            "mousedown": overlayMouseDown,
            "mouseup": overlayMouseUp,
            "mousemove": overlayMouseMove,
            "click": overlayClicked
        });
    }

    window.builderGetFloorPlans = function(cb){
        var req = database.transaction(["layout_images"], "readwrite")
            .objectStore("layout_images")
            .getAll();
        req.onsuccess = cb;
    };

    function builderClearSelection(){
        multi_selected_grid = [];
        selected_grid = [];
        redraw();
    }

    window.builderClearSelection = builderClearSelection;

    function toggleSpaceDisplay(event){
        $(event.currentTarget).toggleClass("builder_space_list_open");
    }

    function overlayMouseDown(event){
        super_debug ? console.debug(arguments.callee.name) : '';
        mouse_down = true;
        var results = getCanvasMouseXandY();

        m_x_start = results[0];
        m_y_start = results[1];
        $(canvas).css("opacity", ".7");
        $(overlay).css("opacity", "1");
    }

    function overlayMouseUp(event){
        super_debug ? console.debug(arguments.callee.name) : '';
        mouse_down = false;
        $(canvas).css("opacity", "1");
        $(overlay).css("opacity", ".2");
        var results = getCanvasMouseXandY();
        var start = getGridXandY(m_x_start, m_y_start);
        var end = getGridXandY(results[0], results[1]);
        console.log(start, end);
        var sx, ex;
        if(start[0] > end[0]) {
            sx = end[0];
            ex = start[0];
        }else{
            sx = start[0];
            ex = end[0];
        }
        var sy, ey;
        if(start[1] > end[1]){
            sy = end[1];
            ey = start[1];
        }else{
            sy = start[1];
            ey = end[1];
        }
        for(var x = sx; x <= ex; x++){
            for(var y = sy; y <= ey; y++){
                if(!multi_selected_grid[x]){
                    multi_selected_grid[x] = [];
                }
                multi_selected_grid[x][y] = "";
            }
        }
    }

    function overlayMouseMove(event){
        super_debug ? console.debug(arguments.callee.name) : '';
        if(mouse_down){
            var results = getCanvasMouseXandY();
            drawBox(m_x_start, m_y_start, results[0], results[1]);
        }
    }

    function overlayClicked(event) {
        debug ? console.debug(arguments.callee.name) : '';
        var results = getCanvasMouseXandY();
        clickCanvas(results[0], results[1]);
    }

    function drawBox(sx, sy, ex, ey){
        debug ? console.debug(arguments.callee.name) : '';
        drawGrid();
        var xl = ex - sx, yl = ey - sy;
        overlay_context.rect(sx, sy, xl, yl);
        overlay_context.stroke();
    }

    function builderLoadFloorplan(plan){
        debug ? console.debug(arguments.callee.name) : '';
        var s = Android.getData(plan);
        loadBuilderData(JSON.parse(s));
    }

    window.builderLoadFloorplan = builderLoadFloorplan;

    window.builderLoadFloorplan2 = function(id){
        debug ? console.debug(arguments.callee.name) : '';
        var s = Android.getData2(plan);
        loadBuilderData(JSON.parse(s));
    }

    function bindTouchEvents(){
        debug ? console.debug(arguments.callee.name) : '';
        $(overlay).off();
        $(overlay).on({
            "touchstart": function(event){
                var c = canvas.getContext('2d');

                var rect = canvas.getBoundingClientRect();
                var touch = event.touches[0];
                var thex = touch.clientX;
                var they = touch.clientY;
                var cx = (thex - rect.left);
                var cy = (they - rect.top);
                touch_cx = cx;
                touch_cy = cy;
            },
            "touchmove": function(event){
                touch_cx = false;
                touch_cy = false;
            },
            "touchend": function(event){
                if(touch_cx && touch_cy) {
                    var xy = clickCanvas(touch_cx, touch_cy);
                    if(Android && Android.setSpace){
                        Android.setSpace(JSON.stringify(xy), $("#builder_selected_box_name").val(), selected_id);
                    }
                }
            }
        });
    }

    function builderToggleGridLines(toggle){
        debug ? console.debug(arguments.callee.name) : '';
        grid_lines_enabled = toggle;

        redraw();
        bindTouchEvents();
    }

    window.builderToggleGridLines = builderToggleGridLines;

    function loadBuilderData(bd){
        debug ? console.debug(arguments.callee.name) : '';
        var loadbd = function(bd){
            var i = document.createElement("img");
            i.src = bd.image;
            i.onload = function (event) {
                full_grid = bd.grid;
                image = i;
                image_name = bd.name;
                floorplanname = bd.floorplanname;
                vgrid_spaces = bd.vgrid_spaces;
                hgrid_spaces = bd.hgrid_spaces;
                selected_id = bd.id;

                imageLoaded(image);
            };
        };
        if(bd) {
            loadbd(bd);
        }else{
            var dbrequest = window.indexedDB.open("BuilderDatabase", 6);
            dbrequest.onsuccess = function(event){
                database = event.target.result;
                var os = database.transaction(["layout_images"], "readwrite")
                    .objectStore("layout_images");
                var req = os.openCursor();
                req.onsuccess = function(event){
                    bd = event.target.result.value;
                    loadbd(bd);
                }
            };
        }
    }

    function downloadFloorplan(event){
        debug ? console.debug(arguments.callee.name) : '';
        var id = parseInt($("#builder_select_existing").val());
        var t = database.transaction(["layout_images"], "readwrite")
            .objectStore("layout_images")
            .get(Number(id));
        t.onsuccess = function(event){
            var data = event.target.result;
            var element = document.createElement('a');
            element.setAttribute('href', 'data:text/plain;charset=utf-8,' + encodeURIComponent(JSON.stringify(data)));
            element.setAttribute('download', "fplan-" + data.id + ".json");

            element.style.display = 'none';
            document.body.appendChild(element);

            element.click();

            document.body.removeChild(element);
        };
    }

    function saveFloorplan(){
        debug ? console.debug(arguments.callee.name) : '';
        var floorplanname = $("#builder_floorplan_name").val();
        var name = image_name;
        var id = parseInt($("#builder_select_existing").val());
        var hs = parseInt($("#builder_hgrid_spaces").val());
        var vs = parseInt($("#builder_vgrid_spaces").val());
        var os = database.transaction(["layout_images"], "readwrite")
            .objectStore("layout_images");
        var req = os.get(id);
        req.onsuccess = function(event){
            var data = event.target.result;
            data.name = name;
            data.floorplanname = floorplanname;
            data.grid = full_grid;
            data.hgrid_spaces = hs;
            data.vgrid_spaces = vs;
            var requp = os.put(data);
            requp.onsuccess = function(event){
                $("#builder_title").css({
                    "background": "darkseagreen"
                });
                setTimeout(function(){
                    $("#builder_title").css({
                        "background": "white"
                    });
                }, 2000);
            };
        };
    }

    function redraw(){
        debug ? console.debug(arguments.callee.name) : '';
        if(multi_selected_grid.length > 0) {
            $("#builder_selected_box").show();
            var sboxname = selected_grid.filter(function(i){
                return i ? true : false;
            }).map(function(i){
                return i;
            });
        }else{
            $("#builder_selected_box").hide();
        }


        drawGrid();
    }

    function setSelectedGrid(x, y, data){
        debug ? console.debug(arguments.callee.name) : '';
        selected_grid = [];
        selected_grid[x] = [];
        selected_grid[x][y] = data;
        $("#builder_selected_box").show();
        $("#builder_selected_box_coords").html("x: " + x + " y: " + y);
        $("#builder_selected_box_name").val(data);
    }

    function setHoverGrid(x, y, data){
        debug ? console.debug(arguments.callee.name) : '';
        if(!hover_grid[x]) {
            hover_grid[x] = [];
        }
        hover_grid[x][y] = data;
    }

    function selectGridFromList(event){
        debug ? console.debug(arguments.callee.name) : '';
        // var x = $(event.target).data("x");
        // var y = $(event.target).data("y");
        var name = $(event.currentTarget).find(".bngs_name").text();

        var x, y;
        $.each($(event.currentTarget).find("li"), function(i, o){
            x = $(o).data("x");
            y = $(o).data("y");
        });
        setSelectedGrid(x, y, name);
        redraw();
    }

    function hoverGridFromList(event){
        debug ? console.debug(arguments.callee.name) : '';
        hover_grid = [];
        $.each($(event.currentTarget).find("li"), function(i, o){
            var x = $(o).data("x");
            var y = $(o).data("y");
            setHoverGrid(x, y, name);
        });

        redraw();
    }

    function removeHoverGridFromList(event){
        debug ? console.debug(arguments.callee.name) : '';
        hover_grid = [];
        redraw();
    }

    function addSpace(event){
        debug ? console.debug(arguments.callee.name) : '';
        var name = $("#builder_selected_box_name").val();
        for(var i = 0; i < multi_selected_grid.length; i++){
            if(multi_selected_grid[i]){
                if(!full_grid[i]){
                    full_grid[i] = [];
                }
                for(var y = 0; y < multi_selected_grid[i].length; y++){
                    if(multi_selected_grid[i][y] || multi_selected_grid[i][y] === ""){
                        full_grid[i][y] = name;
                    }
                }
            }
        }
        multi_selected_grid = [];
        redraw();
        drawFloorPlan();
    }

    function getGridXandY(cx, cy){
        var c = canvas_context;
        var wi = c.canvas.width;
        var he = c.canvas.height;
        var h = hgrid_spaces;
        var v = vgrid_spaces;
        var xsize = wi / h;
        var x = Math.floor(cx / xsize);
        var ysize = he / v;
        var y = Math.floor(cy / ysize);
        return [x, y];
    }

    function clickCanvas(cx, cy){
        var results = getGridXandY(cx, cy);
        var x = results[0], y = results[1];
        var n = $("#builder_selected_box_name").val();
        if(full_grid[x]){
            if(full_grid[x][y] || full_grid[x][y] === ""){
                n = full_grid[x][y];
            }
        }
        setSelectedGrid(x, y, n);
        redraw();
        return [x, y];
    }

    function getCanvasMouseXandY(){
        var c = canvas_context;
        var wi = c.canvas.width;
        var he = c.canvas.height;
        var rect = canvas.getBoundingClientRect();
        var thex = event.clientX;
        var they = event.clientY;
        var cx = (thex - rect.left) / (rect.right-rect.left) * wi;
        var cy = (they - rect.top) / (rect.bottom-rect.top) * he;
        return [cx, cy];
    }

    function spacesChanged(event){
        debug ? console.debug(arguments.callee.name) : '';
        vgrid_spaces = $("#builder_vgrid_spaces").val();
        hgrid_spaces = $("#builder_hgrid_spaces").val();
        imageLoaded();
    }

    function imageLoaded(img){
        debug ? console.debug(arguments.callee.name) : '';
        if(img){
            image = img;
            image_height = img.height;
            image_width = img.width;
        }
        redraw();
        resetZoom();
    }

    function drawGrid() {
        debug ? console.debug(arguments.callee.name) : '';
        var c = canvas_context;
        c.canvas.width = image_width;
        c.canvas.height = image_height;
        c.drawImage(image, 1, 1, image_width, image_height);
        var co = overlay_context;
        co.canvas.width = image_width;
        co.canvas.height = image_height;

        var wi = c.canvas.width;
        var he = c.canvas.height;

        var ho = hgrid_spaces;
        var vi = vgrid_spaces;
        var i = 0;

        var color = $("#builder_grid_color").val();

        if (grid_lines_enabled){
            for (i = 0; i < vi; i++) {
                c.moveTo(0, (he / vi) * i);
                c.lineTo(wi, (he / vi) * i);
                c.strokeStyle = color;
                c.stroke();
            }
        }

        for(i = 0; i < ho; i++){
            if(grid_lines_enabled) {
                c.moveTo((wi / ho) * i, 0);
                c.lineTo((wi / ho) * i, he);
                c.strokeStyle = color;
                c.stroke();
            }

            if(full_grid[i] || full_grid[i] === ""){
                for(var y = 0; y < full_grid[i].length; y++){
                    if(full_grid[i][y] || full_grid[i][y] === ""){
                        co.fillStyle = "red";
                        co.fillRect(
                            (wi / ho) * i,
                            (he / vi) * y,
                            (wi / ho),
                            (he / vi)
                        );
                    }
                }
            }

            if(selected_grid[i] || selected_grid[i] === ""){
                for(var y = 0; y < selected_grid[i].length; y++){
                    if(selected_grid[i][y] || selected_grid[i][y] === ""){
                        c.fillStyle = "green";
                        c.fillRect(
                            (wi / ho) * i,
                            (he / vi) * y,
                            (wi / ho),
                            (he / vi)
                        );
                    }
                }
            }

            if(hover_grid[i] || hover_grid[i] === ""){
                for(var y = 0; y < hover_grid[i].length; y++){
                    if(hover_grid[i][y] || hover_grid[i][y] === ""){
                        co.fillStyle = "gold";
                        co.fillRect(
                            (wi / ho) * i,
                            (he / vi) * y,
                            (wi / ho),
                            (he / vi)
                        );
                    }
                }
            }

            if(multi_selected_grid[i] || multi_selected_grid[i] === ""){
                for(var y = 0; y < multi_selected_grid[i].length; y++){
                    if(multi_selected_grid[i][y] || multi_selected_grid[i][y] === ""){
                        co.fillStyle = "blue";
                        co.fillRect(
                            (wi / ho) * i,
                            (he / vi) * y,
                            (wi / ho),
                            (he / vi)
                        );
                    }
                }
            }
        }
    }

    function deleteExisting(event){
        debug ? console.debug(arguments.callee.name) : '';
        var id = parseInt($("#builder_select_existing").val());
        var t = database.transaction(["layout_images"], "readwrite")
            .objectStore("layout_images")
            .delete(id);
        t.onsuccess = function(event){
            reloadFromDb();
        };
    }

    function resetZoom(){
        debug ? console.debug(arguments.callee.name) : '';
        var w = canvas.width;
        var h = canvas.height;
        var css = {
            "width": parseInt(w)  + "px",
            "height": parseInt(h) + "px"
        };
        $(canvas).css(css);
        $(overlay).css(css);
    }

    function zoomIn(event){
        debug ? console.debug(arguments.callee.name) : '';
        var cw = $(canvas).css("width");
        var ch = $(canvas).css("height");
        var css = {
            "width": parseInt(cw) * 1.1 + "px",
            "height": parseInt(ch) * 1.1 + "px"
        };
        $(canvas).css(css);
        $(overlay).css(css);
    }

    function zoomOut(event){
        debug ? console.debug(arguments.callee.name) : '';
        var cw = $(canvas).css("width");
        var ch = $(canvas).css("height");
        var css = {
            "width": parseInt(cw) * .9 + "px",
            "height": parseInt(ch) * .9 + "px"
        };
        $(canvas).css(css);
        $(overlay).css(css);
    }

    function setImageName(name){
        debug ? console.debug(arguments.callee.name) : '';
        image_name = name;
        $("#builder_table caption#builder_title").html(name);
    }

    function imageChanged(event){
        debug ? console.debug(arguments.callee.name) : '';

        var input = event.target;
        var reader = new FileReader();
        reader.onload = function() {
            var dataURL = reader.result;
            var imageObj = new Image();
            imageObj.src = dataURL;
            imageObj.onload = function(){
                imageLoaded(imageObj);
            };
            setImageName(input.files[0].name);
            var t = database.transaction(["layout_images"], "readwrite")
                .objectStore("layout_images")
                .add({
                    "name": image_name,
                    "image": dataURL,
                    "grid": []
                });
            t.onsuccess = function(event) {
                reloadFromDb(event.target.result);
            };
        };
        reader.readAsDataURL(input.files[0]);
    }

    function selectChanged(event){
        debug ? console.debug(arguments.callee.name) : '';
        displayFloorplan(event.target.value);
    }

    function setFloorplanName(name){
        debug ? console.debug(arguments.callee.name) : '';
        if(name){
            $("#builder_floorplan_name").val(name);
        }else{
            $("#builder_floorplan_name").val("");
        }
    }

    function displayFloorplan(id) {
        debug ? console.debug(arguments.callee.name) : '';
        var t = database.transaction(["layout_images"], "readwrite")
            .objectStore("layout_images")
            .get(Number(id));
        t.onsuccess = function (event) {
            setImageName(event.target.result.name);
            setFloorplanName(event.target.result.floorplanname);
            setHandVSpace(event.target.result.hgrid_spaces, event.target.result.vgrid_spaces);
            var image = document.createElement("img");
            image.src = event.target.result.image;
            image.onload = function (event) {
                imageLoaded(image);
            };
            full_grid = event.target.result.grid;

            drawFloorPlan();
        }
    }

    function setHandVSpace(hspace, vspace){
        debug ? console.debug(arguments.callee.name) : '';
        hgrid_spaces = hspace;
        vgrid_spaces = vspace;
        $("#builder_hgrid_spaces").val(hspace);
        $("#builder_vgrid_spaces").val(vspace);
    }

    function drawFloorPlan(){
        debug ? console.debug(arguments.callee.name) : '';
        $("#builder_named_grid_spaces").html("");
        var names = {};
        for(var x = 0; x < full_grid.length; x++){
            if(full_grid[x]){
                for(var y = 0; y < full_grid[x].length; y++){
                    if(full_grid[x][y] || full_grid[x][y] === ""){
                        var name = full_grid[x][y];
                        if(name.trim() == ""){
                            name = "no name";
                        }
                        if(!names[name]){
                            names[name] = [];
                        }
                        names[name].push([x, y]);
                    }
                }
            }
        }
        $.each(names, function(k, v){
            var left = "<td class='bngs_name'>" + k + "</td>";
            var right = "<td><ul>";
            for(var i = 0; i < v.length; i++){
                right += "<li data-x='" + v[i][0] + "' data-y='" + v[i][1] + "'>" +
                        "X: " + v[i][0] + " Y: " + v[i][1] +
                        "</li>";
            }
            right += "</ul></td>";
            $("#builder_named_grid_spaces").append("<tr>" + left + right + "</tr>");
        });
    }

    function reloadFromDb(id){
        debug ? console.debug(arguments.callee.name) : '';
        $("#builder_select_existing").html("");
        var req = database.transaction(["layout_images"], "readwrite")
            .objectStore("layout_images")
            .getAll();
        req.onsuccess = function(event){
            $(event.target.result).each(function(i, el){
                $("#builder_select_existing").append("<option value='" + el.id + "'>" + el.name + "</option>");
            });
            if(id){
                $("#builder_select_existing").val(id);
            }
            if(event.target.result.length > 0){
                displayFloorplan($("#builder_select_existing").val());
            }
        };
    }

    function dbonsuccess(event) {
        debug ? console.debug(arguments.callee.name) : '';
        database = event.target.result;
        reloadFromDb();
        if(typeof process != "undefined"){
            if(process.mainModule){
                process.mainModule.exports.register(the_builder);
            }
        }
    }

    function dbonerror(event) {
        debug ? console.debug(arguments.callee.name) : '';
        console.error(event);
    }

    function dbonupgradeneeded(event){
        debug ? console.debug(arguments.callee.name) : '';
        database = event.target.result;
        var objectStore = database.createObjectStore("layout_images", { keyPath: "id", autoIncrement: true });
    }

    var the_builder = this;

    $(function(){
        window.indexedDB = window.indexedDB || window.mozIndexedDB || window.webkitIndexedDB || window.msIndexedDB;
        window.IDBTransaction = window.IDBTransaction || window.webkitIDBTransaction || window.msIDBTransaction || {READ_WRITE: "readwrite"};
        window.IDBKeyRange = window.IDBKeyRange || window.webkitIDBKeyRange || window.msIDBKeyRange;
        init();
    });
})(jQuery);