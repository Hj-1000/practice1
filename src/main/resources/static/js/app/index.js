var main = {
    // ==========================================
    // [공통 전역 변수 및 맵 상태 관리]
    // ==========================================
    map: null,
    ps: null,
    infowindow: null,
    markers: [], // 지도에 표시된 마커들을 관리할 배열

    // [추가] 내 주변 1km 조회 시 사용할 전역 객체 변수
    myLocationMarker: null,
    myLocationCircle: null,

    init: function () {
        var _this = this;

        // ==========================================
        // [기존 게시판 관련 이벤트] - 그대로 보존
        // ==========================================
        $('#btn-save').on('click', function () {
            _this.save();
        });

        $('#btn-update').on('click', function () {
            _this.update();
        });

        $('#btn-delete').on('click', function () {
            _this.delete();
        });

        // ==========================================
        // [신규 맛집/리뷰 관련 이벤트 및 초기화]
        // ==========================================
        // 맛집 저장 화면일 때 오늘 날짜 기본 세팅
        if (document.getElementById('visitDate') && !document.getElementById('originRating')) {
            document.getElementById('visitDate').value = new Date().toISOString().substring(0, 10);
        }

        // 맛집 검색 버튼 클릭 이벤트
        $('#btn-search').on('click', function () {
            _this.searchPlaces();
        });

        // 검색창에서 엔터키 쳤을 때도 검색되도록 처리
        $('#search-keyword').on('keypress', function (e) {
            if (e.key === 'Enter') _this.searchPlaces();
        });

        // 리뷰 저장 버튼 클릭 이벤트
        $('#btn-save-review').on('click', function () {
            _this.saveReview();
        });

        // 리뷰 수정 클릭 이벤트
        $('#btn-update-review').on('click', function (){
            _this.updateReview();
        });

        // 리뷰 삭제 클릭 이벤트
        $('#btn-delete-review').on('click', function (){
            _this.deleteReview();
        });

        // [추가] 내 주변 1km 체크박스 이벤트 리스너 등록
        $('#filter-nearby').on('change', function () {
            if ($(this).is(':checked')) {
                // 체크 시 카테고리 라디오 버튼 해제 및 주변 맛집 호출
                $('input[name="btnradio"]').prop('checked', false);
                $('#filter-all').prop('checked', true); // 필요시 초기화
                _this.loadNearbyReviews();
            } else {
                // 체크 해제 시 화면을 원래대로 돌리기 위해 내 위치 요소 제거 후 원본 마커 복원 또는 리로드
                _this.removeMyLocationEffects();
                location.reload();
            }
        });

        // 맛집 수정 화면일 때 기존 별점 데이터 자동 매핑
        if (document.getElementById('originRating')) {
            var originRatingVal = $('#originRating').val(); // 저장되어 있던 점수 (1~5)
            if (originRatingVal) {
                $('#rating').val(originRatingVal); // select 박스의 값을 기존 점수로 강제 변경
            }
        }

        // 카카오 지도 라이브러리가 완전히 로드된 후 initMap을 실행하도록 보장
        if (typeof kakao !== 'undefined' && kakao.maps) {
            kakao.maps.load(function() {
                // 1. 등록/수정 화면용 지도 처리
                if (document.getElementById('map')) {
                    main.initMap();
                }
                // 2. 메인 화면용 전체 지도 대시보드 처리
                if (document.getElementById('main-map')) {
                    main.initMainMap();
                }
            });
        }
    },

    // ==========================================
    // [기존 게시판 비즈니스 로직] - 그대로 보존
    // ==========================================
    save: function () {
        var data = {
            title: $('#title').val(),
            author: $('#author').val(),
            content: $('#content').val()
        };

        $.ajax({
            type: 'POST',
            url: '/api/v1/posts',
            dataType: 'json',
            contentType: 'application/json; charset=utf-8',
            data: JSON.stringify(data)
        }).done(function () {
            alert('글이 등록되었습니다.');
            window.location.href = '/';
        }).fail(function (error) {
            alert(JSON.stringify(error));
        });
    },

    update: function () {
        var data = {
            title: $('#title').val(),
            content: $('#content').val()
        };

        var id = $('#id').val();

        $.ajax({
            type: 'PUT',
            url: '/api/v1/posts/' + id,
            dataType: 'json',
            contentType: 'application/json; charset=utf-8',
            data: JSON.stringify(data)
        }).done(function () {
            alert('글이 수정되었습니다.');
            window.location.href = '/';
        }).fail(function (error) {
            alert(JSON.stringify(error));
        });
    },

    delete: function () {
        var id = $('#id').val();

        console.log("이거 눌리긴 했어")
        $.ajax({
            type: 'DELETE',
            url: '/api/v1/posts/' + id,
            dataType: 'json',
            contentType: 'application/json; charset=utf-8',
        }).done(function () {
            alert('글이 삭제되었습니다.');
            window.location.href = '/';
        }).fail(function (error) {
            alert(JSON.stringify(error));
        });
    },

    // ==========================================
    // [신규 맛집/리뷰 비즈니스 로직 (카카오 지도 연동)]
    // ==========================================

    // 1. 카카오 지도 객체 생성 및 초기화
    initMap: function () {
        var mapContainer = document.getElementById('map');
        var mapOption = {
            center: new kakao.maps.LatLng(37.498, 126.867), // 부천 중심 좌표
            level: 3
        };
        this.map = new kakao.maps.Map(mapContainer, mapOption);
        this.ps = new kakao.maps.services.Places();
        this.infowindow = new kakao.maps.InfoWindow({zIndex: 1});

        // 레이아웃 깨짐 방지: 지도가 로드된 직후 크기를 강제로 다시 맞추도록 깨워줍니다.
        var _this = this;
        setTimeout(function() {
            if(_this.map) {
                _this.map.relayout();
                _this.map.setCenter(new kakao.maps.LatLng(37.498, 126.867));
            }
        }, 100);
    },

    // 2. 입력된 키워드로 카카오 장소 검색 요청
    searchPlaces: function () {
        var keyword = document.getElementById('search-keyword').value;
        if (!keyword.replace(/^\s+|\s+$/g, '')) {
            alert('키워드를 입력해주세요!');
            return;
        }
        this.ps.keywordSearch(keyword, this.placesSearchCB.bind(this));
    },

    // 3. 장소 검색 완료 시 호출되는 콜백 함수
    placesSearchCB: function (data, status, pagination) {
        if (status === kakao.maps.services.Status.OK) {
            this.displayPlaces(data);
        } else if (status === kakao.maps.services.Status.ZERO_RESULT) {
            alert('검색 결과가 존재하지 않습니다.');
        } else if (status === kakao.maps.services.Status.ERROR) {
            alert('검색 결과 중 오류가 발생했습니다.');
        }
    },

    // 4. 검색된 맛집 리스트를 화면에 뿌리고 지도에 마커 표시
    displayPlaces: function (places) {
        var listEl = document.getElementById('places-list');
        var bounds = new kakao.maps.LatLngBounds();
        var _this = this;

        // 새로운 검색을 하면 기존 리스트와 마커를 모두 리셋합니다.
        $('#places-list').empty();
        this.removeAllMarkers();

        for (var i = 0; i < places.length; i++) {
            var placePosition = new kakao.maps.LatLng(places[i].y, places[i].x);

            // 1) 지도 위에 마커를 찍어줍니다.
            var marker = this.addMarker(placePosition);

            // 2) 리스트 엘리먼트 생성
            var itemEl = this.getListItem(i, places[i]);

            bounds.extend(placePosition);
            listEl.appendChild(itemEl);

            // 클로저(IIFE) 패턴으로 각각의 마커와 리스트 아이템 클릭 이벤트 매핑
            (function (place, currentMarker, currentItem) {
                var selectPlaceAction = function () {
                    document.getElementById('kakaoPlaceId').value = place.id;
                    document.getElementById('placeName').value = place.place_name;
                    document.getElementById('category').value = place.category_name.split(' > ')[0];
                    document.getElementById('addressName').value = place.road_address_name ? place.road_address_name : place.address_name;
                    document.getElementById('latitude').value = place.y;
                    document.getElementById('longitude').value = place.x;

                    // 클릭하면 지도가 맛집 위치로 스무스하게 이동하고 정보창 오픈
                    _this.map.panTo(new kakao.maps.LatLng(place.y, place.x));
                    _this.infowindow.setContent('<div style="padding:5px;font-size:12px;color:#333;font-weight:bold;">' + place.place_name + '</div>');
                    _this.infowindow.open(_this.map, currentMarker);

                    // 선택된 아이템 시각화 처리
                    $(currentItem).addClass('active').siblings().removeClass('active');
                };

                // 리스트 아이템 클릭과 지도 위 마커 클릭 둘 다 동일한 데이터 바인딩 액션 수행
                currentItem.onclick = selectPlaceAction;
                kakao.maps.event.addListener(currentMarker, 'click', selectPlaceAction);

            })(places[i], marker, itemEl);
        }

        // 검색된 장소들이 한 화면에 가득 다 들어오도록 지도 축척 자동 조절
        this.map.setBounds(bounds);
    },

    // 마커 생성 및 지도 표시 보조 함수
    addMarker: function (position) {
        var marker = new kakao.maps.Marker({
            position: position
        });
        marker.setMap(this.map);
        this.markers.push(marker);
        return marker;
    },

    // 지도 상의 기존 마커들을 지워주는 보조 함수
    removeAllMarkers: function () {
        this.markers.forEach(function (marker) {
            marker.setMap(null);
        });
        this.markers = [];
    },

    // 리스트 태그 생성 보조 함수
    getListItem: function (index, places) {
        var el = document.createElement('li');
        el.className = 'list-group-item list-group-item-action';
        var address = places.road_address_name ? places.road_address_name : places.address_name;
        el.innerHTML = '<h5>' + places.place_name + '</h5><small class="text-muted">' + address + '</small>';
        el.style.cursor = 'pointer';
        return el;
    },

    // 글로벌 맵 객체 참조 공유를 위해 전역 변수로 할당할 수 있게 구성 유지
    mainMapInstance: null,

    initMainMap: function () {
        var mapContainer = document.getElementById('main-map');
        var mapOption = {
            center: new kakao.maps.LatLng(37.498, 126.867),
            level: 5
        };

        var mainMap = new kakao.maps.Map(mapContainer, mapOption);
        this.mainMapInstance = mainMap; // 전역 조작용 인스턴스 백업

        var infowindow = new kakao.maps.InfoWindow({zIndex: 1});
        var bounds = new kakao.maps.LatLngBounds();

        var rawData = document.getElementById('reviews-json-data') ? document.getElementById('reviews-json-data').value : null;
        if (!rawData) return;

        var reviewList = JSON.parse(rawData);
        if (reviewList.length === 0) return;

        var mainMarkers = [];
        var validMarkerCount = 0;

        // [안전성 확보] 마커 이미지 경로를 카카오의 공식 기본 붉은 핀과 검증된 에셋 주소로 세팅합니다.
        var imageSrcs = {
            '음식점': 'https://t1.daumcdn.net/localimg/localimages/07/mapapidoc/marker_red.png', // 빨간 핀
            '카페': 'https://t1.daumcdn.net/localimg/localimages/07/mapapidoc/markerStar.png',     // 별 모양 핀
            '기타': 'https://t1.daumcdn.net/mapjsapi/images/2x/marker.png'                      // 카카오 기본 푸른 핀
        };

        for (var i = 0; i < reviewList.length; i++) {
            var review = reviewList[i];
            if (!review.latitude || !review.longitude) continue;

            var lat = parseFloat(review.latitude);
            var lng = parseFloat(review.longitude);
            if (isNaN(lat) || isNaN(lng)) continue;

            var moveLatLon = new kakao.maps.LatLng(lat, lng);

            var categoryKey = review.category ? review.category.trim() : '기타';
            if (!imageSrcs[categoryKey]) categoryKey = '기타';

            // 이미지 크기와 옵션 설정
            var imageSize = new kakao.maps.Size(24, 35);
            var markerImage = new kakao.maps.MarkerImage(imageSrcs[categoryKey], imageSize);

            // 마커 객체 생성
            var marker = new kakao.maps.Marker({
                map: mainMap,
                position: moveLatLon,
                image: markerImage
            });

            bounds.extend(moveLatLon);
            validMarkerCount++;

            (function (m, r) {
                kakao.maps.event.addListener(m, 'click', function () {
                    var contentHtml =
                        '<div style="padding:10px; font-size:12px; min-width:150px; color:#333;">' +
                        '   <strong style="display:block; margin-bottom:4px;">' + r.placeName + '</strong>' +
                        '   <span style="color:#f1c40f; display:block; margin-bottom:6px;">' + r.ratingStars + '</span>' +
                        '   <a href="/reviews/update/' + r.id + '" style="color:#3498db; text-decoration:none; font-weight:bold;">후기 보기 ➔</a>' +
                        '</div>';

                    infowindow.setContent(contentHtml);
                    infowindow.open(mainMap, m);
                });
            })(marker, review);

            mainMarkers.push({
                markerInstance: marker,
                category: categoryKey
            });
        }

        if (validMarkerCount > 0) {
            mainMap.setBounds(bounds);
        }

        // 라디오 필터 버튼 클릭 이벤트 (부트스트랩 5에 매핑 보정 - name이 btnradio인 인풋 요소 타겟팅)
        $('input[name="btnradio"]').on('change', function() {
            var selectedId = $(this).attr('id');
            var targetCategory = $(this).data('category');

            // 내 주변 체크박스가 켜져 있다면 비활성화 처리 및 기존 오버레이 삭제
            $('#filter-nearby').prop('checked', false);
            main.removeMyLocationEffects();

            infowindow.close();
            var filterBounds = new kakao.maps.LatLngBounds();
            var activeCount = 0;

            mainMarkers.forEach(function(item) {
                if (selectedId === 'filter-all') {
                    item.markerInstance.setMap(mainMap);
                    filterBounds.extend(item.markerInstance.getPosition());
                    activeCount++;
                } else if (item.category === targetCategory) {
                    item.markerInstance.setMap(mainMap);
                    filterBounds.extend(item.markerInstance.getPosition());
                    activeCount++;
                } else {
                    item.markerInstance.setMap(null);
                }
            });

            if (activeCount > 0) {
                mainMap.setBounds(filterBounds);
            }
        });

        setTimeout(function() {
            mainMap.relayout();
            if (validMarkerCount > 0) mainMap.setBounds(bounds);
        }, 150);
    },

    // ==========================================
    // [추가] 고도화 비즈니스 로직 - 내 주변 맛집 비동기 호출 및 바인딩
    // ==========================================
    loadNearbyReviews: function () {
        var _this = this;

        if (navigator.geolocation) {
            navigator.geolocation.getCurrentPosition(function (position) {
                var lat = position.coords.latitude;
                var lng = position.coords.longitude;
                var radius = 1.0; // 반경 1km 선언

                // [추가된 시각화 기능 호출] 지도 위에 내 위치 핀과 1km 반경 점선을 그립니다.
                _this.drawMyLocationOverlay(lat, lng, radius);

                $.ajax({
                    type: 'GET',
                    url: '/api/v1/reviews/nearby?lat=' + lat + '&lng=' + lng + '&radius=' + radius,
                    dataType: 'json',
                    contentType: 'application/json; charset=utf-8'
                }).done(function (data) {
                    _this.renderReviewCards(data);
                }).fail(function (error) {
                    alert('주변 맛집을 불러오는 데 실패했습니다.');
                    console.log(JSON.stringify(error));
                    $('#filter-nearby').prop('checked', false);
                    _this.removeMyLocationEffects();
                });

            }, function (error) {
                alert('GPS 위치 권한을 승인해 주셔야 주변 맛집 조회가 가능합니다.');
                $('#filter-nearby').prop('checked', false);
                _this.removeMyLocationEffects();
            });
        } else {
            alert('이 브라우저는 GeoLocation 기능을 지원하지 않습니다.');
            $('#filter-nearby').prop('checked', false);
        }
    },

    // [추가] 내 위치 오버레이(마커 + 원) 생성 및 맵 매핑 함수
    drawMyLocationOverlay: function (lat, lng, radiusInKm) {
        var targetMap = this.mainMapInstance;
        if (!targetMap) return;

        // 0. 기존에 남은 오버레이가 있다면 완벽하게 제거
        this.removeMyLocationEffects();

        var centerPosition = new kakao.maps.LatLng(lat, lng);

        // 1. 내 위치 전용 마커 추가 (식당과 구별되는 기본 푸른색 핀 사용)
        var imageSrc = 'https://t1.daumcdn.net/mapjsapi/images/2x/marker.png';
        var imageSize = new kakao.maps.Size(28, 38);
        var markerImage = new kakao.maps.MarkerImage(imageSrc, imageSize);

        this.myLocationMarker = new kakao.maps.Marker({
            map: targetMap,
            position: centerPosition,
            image: markerImage,
            zIndex: 3 // 식당 핀 위에 오도록 순위 상향
        });

        // 2. 반경 1km 원(Circle) 객체 생성 및 반투명 대시선 효과 부여
        var radiusInMeter = radiusInKm * 1000;
        this.myLocationCircle = new kakao.maps.Circle({
            center: centerPosition,
            radius: radiusInMeter,
            strokeWeight: 2,
            strokeColor: '#3498db',
            strokeOpacity: 0.8,
            strokeStyle: 'dashed',
            fillColor: '#ecf0f1',
            fillOpacity: 0.3
        });
        this.myLocationCircle.setMap(targetMap);

        // 3. 내 위치 기준으로 지도를 이동시키고 축척 레벨 최적화
        targetMap.setCenter(centerPosition);
        targetMap.setLevel(5);
    },

    // [추가] 생성된 내 위치 오버레이를 지도에서 소멸시키는 보조 함수
    removeMyLocationEffects: function () {
        if (this.myLocationMarker) {
            this.myLocationMarker.setMap(null);
            this.myLocationMarker = null;
        }
        if (this.myLocationCircle) {
            this.myLocationCircle.setMap(null);
            this.myLocationCircle = null;
        }
    },

    // [추가] 받아온 주변 맛집 데이터를 기반으로 카드를 그려주는 DOM 조작 로직
    renderReviewCards: function (reviews) {
        var cardList = $('#review-card-list');
        var noMessage = $('#no-reviews-message');

        cardList.empty(); // 원래 뿌려져 있던 mustache 서버 렌더링 카드 파괴

        if (!reviews || reviews.length === 0) {
            cardList.hide();
            noMessage.show();
            return;
        }

        noMessage.hide();
        cardList.show();

        // ReviewResponseDto 스펙에 맞춰 필드 출력 조립
        reviews.forEach(function (review) {
            var cardHtml =
                '<div class="col mb-4">' +
                '    <a href="/reviews/update/' + review.id + '" class="text-decoration-none" style="display: block; height: 100%;">' +
                '        <div class="card h-100 shadow-sm border-0" style="border-radius: 12px; background-color: #fcfcfc;">' +
                '            <div class="card-body">' +
                '                <div class="d-flex justify-content-between align-items-start mb-2">' +
                '                    <h5 class="card-title text-truncate font-weight-bold" style="max-width: 70%; color: #222;">' +
                review.placeName +
                '                    </h5>' +
                '                    <span class="badge bg-info text-dark" style="font-size: 0.8rem;">' + review.category + '</span>' +
                '                </div>' +
                '                <p class="card-text text-warning mb-1" style="font-size: 1.1rem;">' +
                (review.ratingStars ? review.ratingStars : '') +
                '                </p>' +
                '                <p class="card-text text-muted small mb-2">' +
                '                    📍 ' + review.addressName +
                '                </p>' +
                '                <hr class="my-2" style="border-top: 1px dashed #ddd;">' +
                '                <p class="card-text text-dark text-break" style="display: -webkit-box; -webkit-line-clamp: 3; -webkit-box-orient: vertical; overflow: hidden; min-height: 4.5rem; line-height: 1.5;">' +
                review.content +
                '                </p>' +
                '            </div>' +
                '            <div class="card-footer bg-transparent border-top-0 text-end">' +
                '                <small class="text-muted">🕒 방문일: ' + (review.visitDate ? review.visitDate : '') + '</small>' +
                '            </div>' +
                '        </div>' +
                '    </a>' +
                '</div>';

            cardList.append(cardHtml);
        });
    },

    // 5. 우측 폼 데이터를 묶어서 스프링 부트 컨트롤러(API)로 POST 요청
    saveReview: function () {
        // 💡 1. JSON 객체 대신 파일과 문자를 동시에 담을 FormData 객체를 생성합니다.
        var formData = new FormData();

        // 필수 값 검증을 위해 임시 변수에 체크
        var kakaoPlaceId = $('#kakaoPlaceId').val();
        var visitDate = $('#visitDate').val();
        var content = $('#content').val();

        if (!kakaoPlaceId) {
            alert('지도 검색을 통해 맛집을 먼저 골라주세요!');
            return;
        }
        if (!visitDate) {
            alert('방문 날짜를 기입해 주세요!');
            return;
        }
        if (!content || !content.trim()) {
            alert('식당 후기 내용을 작성해 주세요!');
            return;
        }

        // 💡 2. Mustache 화면에 새로 추가하신 <input type="file" id="imageFile">에서 실제 파일을 꺼냅니다.
        var fileInput = $('#imageFiles')[0];
        if (fileInput.files.length > 0) {
            for (var i = 0; i < fileInput.files.length; i++) {
                // "imageFiles"라는 이름으로 계속 추가 (컨트롤러의 리스트명과 일치)
                formData.append('imageFiles', fileInput.files[i]);
            }
        }


        // 💡 3. 나머지 텍스트 필드들도 폼 데이터에 하나씩 꽂아줍니다.
        formData.append('kakaoPlaceId', kakaoPlaceId);
        formData.append('placeName', $('#placeName').val());
        formData.append('category', $('#category').val());
        formData.append('addressName', $('#addressName').val());
        formData.append('latitude', parseFloat($('#latitude').val()));
        formData.append('longitude', parseFloat($('#longitude').val()));
        formData.append('rating', parseInt($('#rating').val()));
        formData.append('visitDate', visitDate);
        formData.append('content', content); // 🔍 백엔드에서 null이 뜨지 않도록 정확히 바인딩!

        // 💡 4. Multipart 전송을 위한 전용 설정을 얹어서 AJAX 요청을 보냅니다.
        $.ajax({
            type: 'POST',
            url: '/api/v1/reviews',
            processData: false,      // 🚨 중요: QueryString으로 자동 변환되는 것을 막습니다.
            contentType: false,      // 🚨 중요: 브라우저가 알아서 multipart/form-data 경계선(Boundary)을 잡도록 설정을 해제합니다.
            data: formData           // stringify 하지 않고 객체 그대로 전송
        }).done(function () {
            alert('맛집 기록이 완료되었습니다!');
            window.location.href = '/reviews';
        }).fail(function (error) {
            alert('등록에 실패했습니다. 로그를 확인하세요.');
            console.log(JSON.stringify(error));
        });
    },

    // update 로직 추가
    updateReview: function (){
        var data = {
            rating: parseInt($('#rating').val()),
            content: $('#review-content').val(),
            imageUrl: null
        };

        var id = $('#reviewId').val();

        if (!data.content || !data.content.trim()) {
            alert('후기 내용을 작성해주세요!');
            return;
        }

        $.ajax({
            type: 'PUT',
            url: '/api/v1/reviews/' + id,
            dataType: 'json',
            contentType: 'application/json; charset=utf-8',
            data: JSON.stringify(data)
        }).done(function () {
            alert('맛집 기록이 정상적으로 수정되었습니다.');
            window.location.href = '/reviews';
        }).fail(function (error) {
            alert(JSON.stringify(error));
        });
    },

    // delete 로직 추가
    deleteReview: function (){
        var id = $('#reviewId').val();

        if (!confirm("정말로 이 맛집 기록을 삭제하시겠습니까?")) {
            return;
        }

        $.ajax({
            type: 'DELETE',
            url: '/api/v1/reviews/' + id,
            dataType: 'json',
            contentType: 'application/json; charset=utf-8',
        }).done(function () {
            alert('맛집 기록이 삭제되었습니다.');
            window.location.href = '/reviews';
        }).fail(function (error) {
            alert(JSON.stringify(error));
        });
    }
};

// 메인 초기화 루틴 실행
window.main = main;
window.main.init();