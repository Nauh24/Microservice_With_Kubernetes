# Script to update job categories via API
Write-Host "=== CAP NHAT DANH MUC CONG VIEC ===" -ForegroundColor Green
Write-Host ""

$baseUrl = "http://localhost:8082/api/job-category"

# Get current job categories
Write-Host "Lay danh sach job categories hien tai..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri $baseUrl -UseBasicParsing
    $categories = $response.Content | ConvertFrom-Json
    Write-Host "Tim thay $($categories.Count) job categories" -ForegroundColor Green
} catch {
    Write-Host "Loi khi lay danh sach: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Delete all existing categories
Write-Host ""
Write-Host "Xoa tat ca job categories hien tai..." -ForegroundColor Yellow
foreach ($category in $categories) {
    try {
        $deleteUrl = "$baseUrl/$($category.id)"
        Invoke-WebRequest -Uri $deleteUrl -Method DELETE -UseBasicParsing | Out-Null
        Write-Host "Xoa: $($category.name)" -ForegroundColor Gray
    } catch {
        Write-Host "Loi khi xoa $($category.name): $($_.Exception.Message)" -ForegroundColor Red
    }
}

# Vietnamese job categories data
$vietnameseCategories = @(
    @{name="Cong nhan xay dung"; description="Cong viec lien quan den xay dung, thi cong cac cong trinh dan dung va cong nghiep"},
    @{name="Tho dien"; description="Lap dat, sua chua va bao tri he thong dien trong cac cong trinh xay dung"},
    @{name="Tho nuoc"; description="Lap dat, sua chua he thong cap thoat nuoc, duong ong va thiet bi ve sinh"},
    @{name="Tho han"; description="Han cac ket cau thep, kim loai trong xay dung va san xuat"},
    @{name="Tho son"; description="Son tuong, son ket cau va hoan thien be mat cac cong trinh xay dung"},
    @{name="Cong nhan van chuyen"; description="Van chuyen vat lieu, hang hoa va thiet bi tai cong trinh"},
    @{name="Tho moc"; description="Lam do go, van khuon, cop pha va cac san pham go cho xay dung"},
    @{name="Tho op lat"; description="Op lat gach, da, ceramic cho san nha va tuong cac cong trinh"},
    @{name="Cong nhan don dep"; description="Don dep ve sinh cong trinh, khu vuc lam viec va moi truong xung quanh"},
    @{name="Tho co khi"; description="Sua chua, bao tri may moc thiet bi xay dung va cong nghiep"},
    @{name="Cong nhan bao ve"; description="Bao ve an ninh, trat tu tai cac cong trinh xay dung va khu vuc lam viec"},
    @{name="Tho lam vuon"; description="Cham soc cay xanh, thiet ke va thi cong canh quan"},
    @{name="Cong nhan kho bai"; description="Quan ly, sap xep va bao quan vat tu, thiet bi trong kho"},
    @{name="Tho lai xe"; description="Lai xe tai, xe may xuc, xe can cau va cac phuong tien tai cong trinh"},
    @{name="Cong nhan phu viec"; description="Ho tro cac cong viec phu va lao dong pho thong tai cong trinh"},
    @{name="Nhan vien van phong"; description="Cong viec hanh chinh, van thu, ke toan tai cong trinh va van phong"},
    @{name="Ky thuat vien"; description="Ho tro ky thuat, giam sat chat luong va an toan cong trinh"},
    @{name="Tho may"; description="Van hanh may moc thiet bi xay dung va san xuat chuyen nghiep"},
    @{name="Cong nhan an toan"; description="Dam bao an toan lao dong va phong chong chay no tai cong trinh"},
    @{name="Tho cat gach"; description="Cat, gia cong gach da va vat lieu xay dung theo yeu cau ky thuat"}
)

# Add Vietnamese categories
Write-Host ""
Write-Host "Them cac job categories tieng Viet..." -ForegroundColor Yellow
foreach ($category in $vietnameseCategories) {
    try {
        $body = @{
            name = $category.name
            description = $category.description
            isDeleted = $false
        } | ConvertTo-Json
        
        $headers = @{
            "Content-Type" = "application/json"
        }
        
        Invoke-WebRequest -Uri $baseUrl -Method POST -Body $body -Headers $headers -UseBasicParsing | Out-Null
        Write-Host "Them: $($category.name)" -ForegroundColor Green
    } catch {
        Write-Host "Loi khi them $($category.name): $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "=== HOAN THANH CAP NHAT ===" -ForegroundColor Green
Write-Host "Kiem tra ket qua tai: http://localhost:8082/api/job-category" -ForegroundColor Cyan
