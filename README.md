##  Bài Tập Lớn: Bomberman Game


### Nhóm: MHTQLN


### Thành viên


 - 24022462 Nguyễn Huyền Thương
 - 24022408 Nguyễn Thị Nhật Minh
 - 24022419 Quách Lê Hồng Ngọc


![image](https://github.com/user-attachments/assets/55c540d5-a5aa-4cbc-a4c0-ff8ef8d6d4bd)


### Mô tả về các đối tượng trong trò chơi


Nếu bạn đã từng chơi Bomberman, bạn sẽ cảm thấy quen thuộc với những đối tượng này. Chúng được được chia làm hai loại chính là nhóm đối tượng động (Bomber, Enemy, Bomb) và nhóm đối tượng tĩnh (Grass, Wall, Brick, Portal, Item).
 
- Bomber là nhân vật chính của trò chơi. Bomber có thể di chuyển theo 4 hướng trái/phải/lên/xuống theo sự điều khiển của người chơi.
- Enemy là các đối tượng mà Bomber phải tiêu diệt hết để có thể qua Level. Enemy có thể di chuyển ngẫu nhiên hoặc tự đuổi theo Bomber tùy theo loại Enemy. Các loại Enemy sẽ được mô tả cụ thể ở phần dưới.
- Bomb là đối tượng mà Bomber sẽ đặt và kích hoạt tại các ô Grass. Khi đã được kích hoạt, Bomber và Enemy không thể di chuyển vào vị trí Bomb. Tuy nhiên ngay khi Bomber vừa đặt và kích hoạt Bomb tại ví trí của mình, Bomber có một lần được đi từ vị trí đặt Bomb ra vị trí bên cạnh. Sau khi kích hoạt 2s, Bomb sẽ tự nổ, các đối tượng Flame được tạo ra.
- Grass là đối tượng mà Bomber và Enemy có thể di chuyển xuyên qua, và cho phép đặt Bomb lên vị trí của nó
- Wall là đối tượng cố định, không thể phá hủy bằng Bomb cũng như không thể đặt Bomb lên được, Bomber và Enemy không thể di chuyển vào đối tượng này
- Brick là đối tượng được đặt lên các ô Grass, không cho phép đặt Bomb lên nhưng có thể bị phá hủy bởi Bomb được đặt gần đó. Bomber và Enemy thông thường không thể di chuyển vào vị trí Brick khi nó chưa bị phá hủy.
- Portal là đối tượng được giấu phía sau một đối tượng Brick. Khi Brick đó bị phá hủy, Portal sẽ hiện ra và nếu tất cả Enemy đã bị tiêu diệt thì người chơi có thể qua Level khác bằng cách di chuyển vào vị trí của Portal.
Trong quá trình chơi, sẽ có 1 tỷ lệ nhất định người chơi có thể tìm thấy các Item được giấu phía sau Brick và chỉ hiện ra khi Brick bị phá hủy. Bomber có thể sử dụng Item bằng cách di chuyển vào vị trí của Item. Các Item trong game:
- SpeedItem Khi sử dụng Item này, Bomber sẽ được tăng vận tốc di chuyển thêm một giá trị thích hợp
- FlameItem Item này giúp tăng phạm vi ảnh hưởng của Bomb khi nổ (độ dài các Flame lớn hơn)
- BombItem Thông thường, nếu không có đối tượng Bomb nào đang trong trạng thái kích hoạt, Bomber sẽ được đặt và kích hoạt duy nhất một đối tượng Bomb. Item này giúp tăng số lượng Bomb có thể đặt thêm một.
Có nhiều loại Enemy trong Bomberman, tuy nhiên trong phiên bản này chỉ yêu cầu cài đặt hai loại Enemy dưới đây (nếu cài đặt thêm các loại khác sẽ được cộng thêm điểm):
- Balloom, Pass, Ovapi, Minvo, Kondoria là Enemy đơn giản nhất, di chuyển ngẫu nhiên với vận tốc cố định
- Oneal di chuyển "thông minh" hơn so với Balloom, biết đuổi theo Bomber
- Doll có tốc độ di chuyển thay đổi, lúc nhanh, lúc chậm
### Mô tả game play, xử lý va chạm và xử lý bom nổ
- Trong một màn chơi, Bomber sẽ được người chơi di chuyển, đặt và kích hoạt Bomb với mục tiêu chính là tiêu diệt tất cả Enemy và tìm ra vị trí Portal để có thể qua màn mới
- Bomber sẽ bị giết khi va chạm với Enemy hoặc thuộc phạm vi Bomb nổ. Lúc đấy trò chơi kết thúc.
- Enemy bị tiêu diệt khi thuộc phạm vi Bomb nổ
- Một đối tượng thuộc phạm vi Bomb nổ có nghĩa là đối tượng đó va chạm với một trong các tia lửa được tạo ra tại thời điểm một đối tượng Bomb nổ.
- Khi Bomb nổ, một Flame trung tâm tại vị trí Bomb nổ và bốn Flame tại bốn vị trí ô đơn vị xung quanh vị trí của Bomb xuất hiện theo bốn hướng trên/dưới/trái/phải. Độ dài bốn Flame xung quanh mặc định là 1 đơn vị, được tăng lên khi Bomber sử dụng các FlameItem.
- Khi các Flame xuất hiện, nếu có một đối tượng thuộc loại Brick/Wall nằm trên vị trí một trong các Flame thì độ dài Flame đó sẽ được giảm đi để sao cho Flame chỉ xuất hiện đến vị trí đối tượng Brick/Wall theo hướng xuất hiện. Lúc đó chỉ có đối tượng Brick/Wall bị ảnh hưởng bởi Flame, các đối tượng tiếp theo không bị ảnh hưởng. Còn nếu vật cản Flame là một đối tượng Bomb khác thì đối tượng Bomb đó cũng sẽ nổ ngay lập tức.


### Cài đặt
    1. Sao chép dự án từ kho lưu trữ.
    2. Mở dự án trong IDE.
    3. Chạy dự án.
    4. Nếu bạn muốn thay đổi dữ liệu, bạn có thể thay đổi các tệp level.txt


### Tham khảo: <img src="https://raw.githubusercontent.com/gilbarbara/logos/master/logos/github-icon.svg" alt="GitHub" width="24" height="24"> <img src="data:image/png;base64,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" alt="Logo" width="24" height="24">







