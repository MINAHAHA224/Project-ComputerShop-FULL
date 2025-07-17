package vn.javaweb.ComputerShop.service.product;

import org.springframework.web.multipart.MultipartFile;
import vn.javaweb.ComputerShop.domain.dto.request.CartDetailsListDTO;
import vn.javaweb.ComputerShop.domain.dto.request.ProductCreateRqDTO;
import vn.javaweb.ComputerShop.domain.dto.request.ProductFilterDTO;
import vn.javaweb.ComputerShop.domain.dto.request.ProductUpdateRqDTO;
import vn.javaweb.ComputerShop.domain.dto.response.*;
import vn.javaweb.ComputerShop.domain.dto.response.ResponseBody;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

 public interface ProductService {
     ProductFilterRpDTO handleShowDataProductFilter(ProductFilterDTO productFilterDTO);
     ProductFilterAdRpDTO handleShowDataProductAdmin(Optional<String> pageOptional);
     ProductUpdateRqDTO handleGetProductUpdate (Long id);
     ResponseBody handleCreateProduct (ProductCreateRqDTO productCreateRqDTO , MultipartFile file);
     ResponseBody handleUpdateProduct (ProductUpdateRqDTO productUpdateRqDTO , MultipartFile file);
     List<ProductRpDTO> getAllProductView();
     ResponseBody handleDeleteProduct (Long id);
     ProductDetailRpDTO handleGetProductRpAdmin(Long id);
     ProductDetailRpDTO handleGetProductDetail(long id);
     ResponseBody handleConfirmCheckout(CartDetailsListDTO cartDetailsListDTO , Locale locale);
}
